package dev.twme.blocket.managers;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.LightData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.events.OnBlockChangeSendEvent;
import dev.twme.blocket.exceptions.ChunkProcessingException;
import dev.twme.blocket.models.Audience;
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.View;
import dev.twme.blocket.processors.ChunkPacketData;
import dev.twme.blocket.processors.ChunkProcessingContext;
import dev.twme.blocket.processors.ChunkProcessorFactory;
import dev.twme.blocket.types.BlocketChunk;
import dev.twme.blocket.types.BlocketPosition;
import dev.twme.blocket.utils.LRUCache;
import dev.twme.blocket.utils.ObjectPool;
import dev.twme.blocket.utils.PerformanceMonitor;
import io.papermc.paper.math.Position;
import lombok.Getter;
import lombok.NonNull;

/**
 * Manages block changes and caching for virtual blocks across different players and views.
 * This manager handles the complex task of tracking which blocks each player should see,
 * managing view-specific block caches, and sending appropriate packet updates to clients.
 *
 * <p>The BlockChangeManager maintains per-player block caches that combine blocks from
 * multiple views, handles incremental updates when views are added/removed, and optimizes
 * packet sending through chunk-based processing and asynchronous operations.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Per-player block change caching</li>
 *   <li>View-aware block management</li>
 *   <li>Asynchronous chunk packet processing</li>
 *   <li>Efficient memory management</li>
 *   <li>Incremental block updates</li>
 * </ul>
 *
 * @author TWME-TW
 * @version 1.0.1
 * @since 1.0.0
 */
@Getter
public class BlockChangeManager {
    private final BlocketAPI api;
    private final ConcurrentHashMap<UUID, BukkitTask> blockChangeTasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BlockData, Integer> blockDataToId = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    private final Map<UUID, Map<BlocketChunk, Map<BlocketPosition, BlockData>>> playerBlockChanges = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Map<BlocketChunk, Set<BlocketPosition>>>> playerViewBlocks = new ConcurrentHashMap<>();
    private final LRUCache<BlocketChunk, Map<BlocketPosition, BlockData>> blockDataCache;
    private final ChunkProcessorFactory chunkProcessorFactory;
    private final ObjectPool<Map<BlocketPosition, BlockData>> blockDataMapPool;
    private final ObjectPool<List<BaseChunk>> chunkListPool;
    private final ObjectPool<byte[]> lightDataArrayPool;
    private final ObjectPool<BitSet> bitSetPool;
    private final ObjectPool<byte[][]> lightArrayPool;
    private final ObjectPool<AbstractMap.SimpleEntry<BlocketChunk, Map<BlocketPosition, BlockData>>> entryPool;
    private final PerformanceMonitor performanceMonitor;
    private BukkitTask cacheCleanupTask;

    /**
     * Creates a new BlockChangeManager with the specified BlocketAPI instance.
     * Initializes thread pools and data structures for managing block changes.
     *
     * @param api The BlocketAPI instance this manager belongs to
     */
    public BlockChangeManager(BlocketAPI api) {
        this.api = api;
        this.blockDataCache = new LRUCache<>(api.getConfig().getBlockCacheSize());
        this.chunkProcessorFactory = new ChunkProcessorFactory();
        // Uses configured object pool size to improve performance tuning flexibility
        int maxPoolSize = api.getConfig().getMaxObjectPoolSize();
        this.blockDataMapPool = new ObjectPool<>(HashMap::new, maxPoolSize / 2);
        this.chunkListPool = new ObjectPool<>(ArrayList::new, maxPoolSize / 5);
        this.lightDataArrayPool = new ObjectPool<>(() -> new byte[2048], maxPoolSize);
        // Extended object pool support: Add pooling for more repeatedly created objects, using configured size
        this.bitSetPool = new ObjectPool<>(() -> new BitSet(32), maxPoolSize / 3);
        this.lightArrayPool = new ObjectPool<>(() -> new byte[32][2048], maxPoolSize / 5);
        this.entryPool = new ObjectPool<>(() -> new AbstractMap.SimpleEntry<>(null, null), maxPoolSize / 2);
        this.performanceMonitor = new PerformanceMonitor();
        this.executorService = new ThreadPoolExecutor(
                0,
                Runtime.getRuntime().availableProcessors() * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r, "Blocket-BlockChange-Worker");
                    t.setDaemon(true);
                    return t;
                }
        );
        
        // Start periodic cache cleanup task to prevent memory leaks
        startCacheCleanupTask();
    }

    /**
     * Starts a periodic task to clean up stale cache entries and prevent memory leaks.
     * This task runs every 5 minutes and removes empty entries from player caches.
     */
    private void startCacheCleanupTask() {
        cacheCleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(api.getOwnerPlugin(), () -> {
            try {
                performCacheCleanup();
            } catch (Exception e) {
                api.getOwnerPlugin().getLogger().warning("Error during cache cleanup: " + e.getMessage());
            }
        }, 6000L, 6000L); // Run every 5 minutes (6000 ticks)
    }

    /**
     * Performs cache cleanup by removing empty entries and orphaned data.
     * This helps prevent memory leaks from accumulating over time.
     */
    private void performCacheCleanup() {
        AtomicInteger removedEntries = new AtomicInteger(0);
        
        // Clean up empty player block changes
        playerBlockChanges.entrySet().removeIf(entry -> {
            Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerCache = entry.getValue();
            if (playerCache.isEmpty()) {
                removedEntries.incrementAndGet();
                return true;
            }
            
            // Remove empty chunk maps within player cache
            playerCache.entrySet().removeIf(chunkEntry -> {
                Map<BlocketPosition, BlockData> chunkMap = chunkEntry.getValue();
                return chunkMap == null || chunkMap.isEmpty();
            });
            
            return playerCache.isEmpty();
        });
        
        // Clean up empty player view blocks
        playerViewBlocks.entrySet().removeIf(entry -> {
            Map<String, Map<BlocketChunk, Set<BlocketPosition>>> viewMap = entry.getValue();
            if (viewMap.isEmpty()) {
                return true;
            }
            
            // Remove empty view entries
            viewMap.entrySet().removeIf(viewEntry -> {
                Map<BlocketChunk, Set<BlocketPosition>> chunkMap = viewEntry.getValue();
                if (chunkMap == null || chunkMap.isEmpty()) {
                    return true;
                }
                
                // Remove empty chunk entries within view
                chunkMap.entrySet().removeIf(chunkEntry -> {
                    Set<BlocketPosition> positions = chunkEntry.getValue();
                    return positions == null || positions.isEmpty();
                });
                
                return chunkMap.isEmpty();
            });
            
            return viewMap.isEmpty();
        });
        
        if (removedEntries.get() > 0) {
            api.getOwnerPlugin().getLogger().info(String.format("Cache cleanup removed %d empty entries", removedEntries.get()));
        }
    }

    /**
     * Validates cache consistency for a specific player.
     * This method checks if the player's cache data is consistent and removes any orphaned entries.
     *
     * @param playerUUID The UUID of the player to validate cache for
     */
    public void validatePlayerCache(UUID playerUUID) {
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerCache = playerBlockChanges.get(playerUUID);
        Map<String, Map<BlocketChunk, Set<BlocketPosition>>> viewMap = playerViewBlocks.get(playerUUID);
        
        if (playerCache == null || viewMap == null) {
            return;
        }
        
        // Remove chunks from player cache that are not referenced in any view
        Set<BlocketChunk> referencedChunks = viewMap.values().stream()
                .flatMap(chunkMap -> chunkMap.keySet().stream())
                .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        
        playerCache.entrySet().removeIf(entry -> {
            BlocketChunk chunk = entry.getKey();
            if (!referencedChunks.contains(chunk)) {
                blockDataCache.remove(chunk);
                return true;
            }
            return false;
        });
    }

    /**
     * Initializes block change tracking for a new player.
     * Creates empty data structures for the player's block changes and view tracking.
     *
     * @param player The player to initialize tracking for
     */
    public void initializePlayer(@NonNull Player player) {
        playerBlockChanges.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        playerViewBlocks.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
    }

    /**
     * Removes all tracking data for a player when they disconnect or leave a stage.
     * Cleans up memory by removing the player's block change cache and view tracking.
     *
     * @param player The player to remove tracking for
     */
    public void removePlayer(@NonNull Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerChanges = playerBlockChanges.remove(playerUUID);
        if (playerChanges != null) {
            playerChanges.keySet().forEach(blockDataCache::remove);
        }
        playerViewBlocks.remove(playerUUID);
    }

    /**
     * Clears all cached data associated with a specific stage.
     * This method removes all player caches that contain blocks from views belonging to the specified stage.
     * Should be called when a stage is deleted to prevent memory leaks and stale cache data.
     *
     * @param stage The stage whose cache data should be cleared
     */
    public void clearStageCache(@NonNull Stage stage) {
        if (stage == null) {
            return;
        }

        // Get all view names from the stage
        Set<String> stageViewNames = stage.getViews().stream()
                .map(View::getName)
                .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        if (stageViewNames.isEmpty()) {
            return;
        }

        // Clear cache data for all players
        playerViewBlocks.forEach((playerUUID, viewMap) -> {
            // Remove all views belonging to this stage from player's view tracking
            stageViewNames.forEach(viewName -> {
                Map<BlocketChunk, Set<BlocketPosition>> removedViewBlocks = viewMap.remove(viewName);
                if (removedViewBlocks != null) {
                    // Also remove corresponding blocks from player's block changes cache
                    Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerCache = playerBlockChanges.get(playerUUID);
                    if (playerCache != null) {
                        removedViewBlocks.forEach((chunk, positions) -> {
                            Map<BlocketPosition, BlockData> chunkMap = playerCache.get(chunk);
                            if (chunkMap != null) {
                                positions.forEach(chunkMap::remove);
                                if (chunkMap.isEmpty()) {
                                    playerCache.remove(chunk);
                                }
                            }
                            // Clear chunk from cache to force regeneration
                            blockDataCache.remove(chunk);
                        });
                    }
                }
            });
        });

        // Clear any cached chunk data that might belong to this stage
        // Since we can't directly map chunks to stages, we clear all cache to be safe
        blockDataCache.clear();

        api.getOwnerPlugin().getLogger().info(String.format("Cleared cache data for stage: %s", stage.getName()));
    }

    /**
     * Clears cached data for a specific view across all players.
     * This method is called when a view is removed from a stage to ensure cache consistency.
     *
     * @param viewName The name of the view to clear from caches
     */
    public void clearViewCache(@NonNull String viewName) {
        if (viewName == null || viewName.isEmpty()) {
            return;
        }

        playerViewBlocks.forEach((playerUUID, viewMap) -> {
            Map<BlocketChunk, Set<BlocketPosition>> removedViewBlocks = viewMap.remove(viewName);
            if (removedViewBlocks != null) {
                // Also remove corresponding blocks from player's block changes cache
                Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerCache = playerBlockChanges.get(playerUUID);
                if (playerCache != null) {
                    removedViewBlocks.forEach((chunk, positions) -> {
                        Map<BlocketPosition, BlockData> chunkMap = playerCache.get(chunk);
                        if (chunkMap != null) {
                            positions.forEach(chunkMap::remove);
                            if (chunkMap.isEmpty()) {
                                playerCache.remove(chunk);
                            }
                        }
                        // Clear chunk from cache to force regeneration
                        blockDataCache.remove(chunk);
                    });
                }
            }
        });
    }

    /**
     * Hide a view from a player.
     * This removes the view's blocks from the player's cache and then sends updated blocks so the player no longer sees them.
     * @param player The player to hide the view from
     * @param view The view to hide from the player
     */
    public void hideView(@NonNull Player player, @NonNull View view) {
        removeViewFromPlayer(player, view);
        view.getStage().sendBlocksToAudience();
    }

    /**
     * Add a view's blocks to a player's cache in place.
     * @param player The player to add the view to
     * @param view The view to add to the player's cache
     */
    public void addViewToPlayer(@NonNull Player player, @NonNull View view) {
        UUID playerUUID = player.getUniqueId();
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerCache = playerBlockChanges.get(playerUUID);
        Map<String, Map<BlocketChunk, Set<BlocketPosition>>> viewMap = playerViewBlocks.get(playerUUID);

        if (playerCache == null || viewMap == null) return;

        Map<BlocketChunk, Set<BlocketPosition>> viewBlockPositions = new ConcurrentHashMap<>();

        view.getBlocks().forEach((chunk, chunkData) -> {
            Map<BlocketPosition, BlockData> chunkMap = playerCache.computeIfAbsent(chunk, c -> new ConcurrentHashMap<>());
            chunkData.forEach((pos, blockData) -> {
                chunkMap.put(pos, blockData);
                viewBlockPositions.computeIfAbsent(chunk, c -> ConcurrentHashMap.newKeySet()).add(pos);
            });
            blockDataCache.remove(chunk);
        });

        viewMap.put(view.getName(), viewBlockPositions);
    }

    /**
     * Remove a view's blocks from a player's cache in place.
     * @param player The player to remove the view from
     * @param view The view to remove from the player's cache
     */
    public void removeViewFromPlayer(@NonNull Player player, @NonNull View view) {
        UUID playerUUID = player.getUniqueId();
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerCache = playerBlockChanges.get(playerUUID);
        Map<String, Map<BlocketChunk, Set<BlocketPosition>>> viewMap = playerViewBlocks.get(playerUUID);

        if (playerCache == null || viewMap == null) return;

        Map<BlocketChunk, Set<BlocketPosition>> viewBlocks = viewMap.remove(view.getName());
        if (viewBlocks == null) return;

        viewBlocks.forEach((chunk, positions) -> {
            Map<BlocketPosition, BlockData> chunkMap = playerCache.get(chunk);
            if (chunkMap != null) {
                positions.forEach(chunkMap::remove);
                if (chunkMap.isEmpty()) {
                    playerCache.remove(chunk);
                }
            }
            blockDataCache.remove(chunk);
        });
    }

    /**
     * Apply a single block change for a player. If data is null, remove block.
     * @param player The player to apply the block change for
     * @param chunk The chunk containing the block
     * @param pos The position of the block
     * @param data The block data to apply, or null to remove the block
     * @param viewName The name of the view this block change belongs to
     */
    public void applyBlockChange(@NonNull Player player, @NonNull BlocketChunk chunk, @NonNull BlocketPosition pos, BlockData data, String viewName) {
        UUID playerUUID = player.getUniqueId();
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerCache = playerBlockChanges.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        Map<BlocketPosition, BlockData> chunkMap = playerCache.computeIfAbsent(chunk, c -> new ConcurrentHashMap<>());
        Map<String, Map<BlocketChunk, Set<BlocketPosition>>> viewMap = playerViewBlocks.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());

        blockDataCache.remove(chunk);

        if (data == null) {
            chunkMap.remove(pos);
            if (chunkMap.isEmpty()) {
                playerCache.remove(chunk);
            }

            if (viewName != null) {
                Optional.ofNullable(viewMap.get(viewName)).ifPresent(viewChunks -> {
                    Optional.ofNullable(viewChunks.get(chunk)).ifPresent(positions -> {
                        positions.remove(pos);
                        if (positions.isEmpty()) {
                            viewChunks.remove(chunk);
                            if (viewChunks.isEmpty()) {
                                viewMap.remove(viewName);
                            }
                        }
                    });
                });
            }
        } else {
            chunkMap.put(pos, data);
            if (viewName != null) {
                viewMap.computeIfAbsent(viewName, k -> new ConcurrentHashMap<>())
                        .computeIfAbsent(chunk, c -> ConcurrentHashMap.newKeySet())
                        .add(pos);
            }
        }
    }

    /**
     * Retrieve block changes for a player filtered by requested chunks.
     */
    private Map<BlocketChunk, Map<BlocketPosition, BlockData>> getBlockChangesForPlayer(@NonNull Player player, @NonNull Collection<BlocketChunk> chunks) {
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> changes = playerBlockChanges.get(player.getUniqueId());
        if (changes == null || changes.isEmpty() || chunks.isEmpty()) {
            return Collections.emptyMap();
        }

        return chunks.stream()
                .map(chunk -> {
                    Map<BlocketPosition, BlockData> data = blockDataCache.get(chunk);
                    if (data == null) {
                        data = changes.get(chunk);
                        if (data != null && !data.isEmpty()) {
                            blockDataCache.put(chunk, data);
                        }
                    }
                    return data != null && !data.isEmpty() ? new AbstractMap.SimpleEntry<>(chunk, data) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Sends block changes for a stage to an audience without unloading.
     * @param stage The stage containing the blocks to send
     * @param audience The audience to send the block changes to
     * @param chunks The chunks containing the blocks to send
     */
    public void sendBlockChanges(@NonNull Stage stage, @NonNull Audience audience, @NonNull Collection<BlocketChunk> chunks) {
        sendBlockChanges(stage, audience, chunks, false);
    }

    /**
     * Sends block changes for a stage to an audience with optional unloading.
     * @param stage The stage containing the blocks to send
     * @param audience The audience to send the block changes to
     * @param chunks The chunks containing the blocks to send
     * @param unload Whether to unload the chunks after sending
     */
    public void sendBlockChanges(@NonNull Stage stage, @NonNull Audience audience, @NonNull Collection<BlocketChunk> chunks, boolean unload) {
        List<BlocketChunk> chunkList = new ArrayList<>(chunks);
        audience.getOnlinePlayers().stream()
                .filter(player -> player.isOnline() && player.getWorld().equals(stage.getWorld()))
                .forEach(player -> {
                    Map<BlocketChunk, Map<BlocketPosition, BlockData>> blockChanges = getBlockChangesForPlayer(player, chunks);
                    Bukkit.getScheduler().runTask(api.getOwnerPlugin(), () -> new OnBlockChangeSendEvent(stage, blockChanges).callEvent());

                    AtomicInteger chunkIndex = new AtomicInteger(0);
                    
                    // Thread safety: Check and cancel existing tasks before adding new tasks to prevent task overlap and leaks
                    UUID playerUUID = player.getUniqueId();
                    BukkitTask existingTask = blockChangeTasks.get(playerUUID);
                    if (existingTask != null && !existingTask.isCancelled()) {
                        existingTask.cancel();
                    }
                    
                    BukkitTask task = Bukkit.getScheduler().runTaskTimer(api.getOwnerPlugin(), () -> {
                        if (chunkIndex.get() >= chunkList.size()) {
                            cancelTask(playerUUID);
                            return;
                        }
                        for (int i = 0; i < stage.getChunksPerTick() && chunkIndex.get() < chunkList.size(); i++) {
                            sendChunkPacket(player, chunkList.get(chunkIndex.getAndIncrement()), unload);
                        }
                    }, 0L, 1L);
                    blockChangeTasks.put(playerUUID, task);
                });
    }

    /**
     * Sends multiple block changes to a single player.
     * @param player The player to send block changes to
     * @param blocks The set of block positions to send changes for
     */
    public void sendMultiBlockChange(@NonNull Player player, @NonNull Set<BlocketPosition> blocks) {
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerChanges = playerBlockChanges.get(player.getUniqueId());
        if (playerChanges == null) return;

        final Map<Position, BlockData> blocksToSend = blocks.stream()
                .map(pos -> {
                    Map<BlocketPosition, BlockData> chunkData = playerChanges.get(pos.toBlocketChunk());
                    if (chunkData != null) {
                        BlockData blockData = chunkData.get(pos);
                        if (blockData != null) {
                            return new AbstractMap.SimpleEntry<>(pos.toPosition(), blockData);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!blocksToSend.isEmpty()) {
            player.sendMultiBlockChange(blocksToSend);
        }
    }

    private void cancelTask(UUID playerId) {
        Optional.ofNullable(blockChangeTasks.remove(playerId)).ifPresent(BukkitTask::cancel);
    }

    /**
     * Sends a chunk packet to the specified player.
     *
     * @param player The target player to send the chunk packet to
     * @param chunk The chunk to be sent
     * @param unload Whether this is an unload operation
     */
    public void sendChunkPacket(@NonNull Player player, @NonNull BlocketChunk chunk, boolean unload) {
        executorService.submit(() -> processAndSendChunk(player, chunk, unload));
    }

    /**
     * Processes and sends a chunk packet.
     *
     * @param player The target player
     * @param chunk The chunk to process
     * @param unload Whether this is an unload operation
     */
    /**
     * Main method for processing and sending chunk packets
     * Refactored into smaller methods to reduce complexity and improve maintainability
     *
     * @param player Target player
     * @param chunk Chunk to process
     * @param unload Whether this is an unload operation
     */
    private void processAndSendChunk(Player player, BlocketChunk chunk, boolean unload) {
        try (PerformanceMonitor.Timer timer = performanceMonitor.startTimer("processAndSendChunk")) {
            executeChunkProcessingPipeline(player, chunk, unload);
        } catch (ChunkProcessingException e) {
            handleKnownChunkProcessingError(player, chunk, e);
        } catch (Exception e) {
            handleUnknownChunkProcessingError(player, chunk, e);
        }
    }

    /**
     * Execute all steps of the chunk processing pipeline
     * Decompose complex processing logic into clear steps
     *
     * @param player Target player
     * @param chunk Chunk to process
     * @param unload Whether this is an unload operation
     * @throws ChunkProcessingException If an error occurs during processing
     */
    private void executeChunkProcessingPipeline(Player player, BlocketChunk chunk, boolean unload) throws ChunkProcessingException {
        // Step 1: Validate input parameters
        validateChunkProcessingInputs(player, chunk);
        
        // Step 2: Create processing context
        ChunkProcessingContext context = createProcessingContext(player, chunk, unload);
        
        // Step 3: Create chunk packet data
        ChunkPacketData packetData = createChunkPacketData(context);
        
        // Step 4: Send packet to client
        sendChunkPackets(context.getPacketUser(), chunk, packetData);
    }

    /**
     * Handle known chunk processing exceptions
     * Separate exception handling logic to improve code clarity
     *
     * @param player Related player
     * @param chunk Related chunk
     * @param e Chunk processing exception
     */
    private void handleKnownChunkProcessingError(Player player, BlocketChunk chunk, ChunkProcessingException e) {
        performanceMonitor.incrementCounter("chunkProcessingErrors");
        handleChunkProcessingError(player, chunk, e);
    }

    /**
     * Handle unknown chunk processing exceptions
     * Separate exception handling logic to improve code clarity
     *
     * @param player Related player
     * @param chunk Related chunk
     * @param e Unexpected exception
     */
    private void handleUnknownChunkProcessingError(Player player, BlocketChunk chunk, Exception e) {
        performanceMonitor.incrementCounter("unexpectedErrors");
        handleUnexpectedError(player, chunk, e);
    }

    /**
     * Validates the inputs for chunk processing.
     *
     * @param player The player
     * @param chunk The chunk
     * @throws ChunkProcessingException if inputs are invalid
     */
    private void validateChunkProcessingInputs(Player player, BlocketChunk chunk) throws ChunkProcessingException {
        if (player == null || !player.isOnline() || player.getWorld() == null) {
            throw new ChunkProcessingException("Player is invalid or offline.");
        }
        if (chunk == null) {
            throw new ChunkProcessingException("Chunk cannot be null.");
        }
    }

    /**
     * Creates a chunk processing context.
     *
     * @param player The player
     * @param chunk The chunk
     * @param unload Whether to unload
     * @return The processing context
     * @throws ChunkProcessingException if context creation fails
     */
    private ChunkProcessingContext createProcessingContext(Player player, BlocketChunk chunk, boolean unload) throws ChunkProcessingException {
        try {
            User packetUser = PacketEvents.getAPI().getPlayerManager().getUser(player);
            if (packetUser == null) {
                throw new ChunkProcessingException("Failed to get PacketUser for player.");
            }
            Map<BlocketPosition, BlockData> customBlockData = unload ? null : getBlockChangesForPlayer(player, Collections.singleton(chunk)).get(chunk);
            return new ChunkProcessingContext(player, chunk, packetUser, customBlockData, unload);
        } catch (Exception e) {
            throw new ChunkProcessingException("Error creating processing context.", e);
        }
    }

    /**
     * Creates chunk packet data.
     *
     * @param context The processing context
     * @return The chunk packet data
     * @throws ChunkProcessingException if packet data creation fails
     */
    private ChunkPacketData createChunkPacketData(ChunkProcessingContext context) throws ChunkProcessingException {
        try (PerformanceMonitor.Timer timer = performanceMonitor.startTimer("createChunkPacketData")) {
            if (context.isUnload()) {
                performanceMonitor.incrementCounter("emptyChunkPackets");
                return createEmptyChunkPacketData(context);
            }

            // Use configuration to determine lighting processing strategy
            boolean preserveLighting = api.getConfig().isPreserveOriginalLighting();
            ChunkProcessorFactory.ChunkProcessingOptions options = new ChunkProcessorFactory.ChunkProcessingOptions(context.getPacketUser())
                .useEmptyLighting(!preserveLighting)
                .preserveOriginalLighting(preserveLighting);
            Column column = chunkProcessorFactory.createChunkColumn(context.getPlayer(), context.getChunk(), context.getCustomBlockData(), options);
            
            // Determine light data processing strategy based on configuration
            LightData lightData;
            if (preserveLighting) {
                // Extract correct light data from Column
                lightData = extractLightDataFromColumn(column, context);
            } else {
                // Backward compatibility: use empty light data
                lightData = createEmptyLightData(context);
            }

            performanceMonitor.incrementCounter("fullChunkPackets");
            return new ChunkPacketData(column, lightData);
        } catch (Exception e) {
            throw new ChunkProcessingException("Error creating chunk packet data.", e);
        }
    }

    /**
     * Creates empty chunk packet data for unload operations.
     *
     * @param context The processing context
     * @return Empty chunk packet data
     */
    private ChunkPacketData createEmptyChunkPacketData(ChunkProcessingContext context) {
        BaseChunk[] emptyChunks = new BaseChunk[0];
        Column emptyColumn = new Column(context.getChunk().x(), context.getChunk().z(), true, emptyChunks, null);
        LightData emptyLightData = createEmptyLightData(context);
        return new ChunkPacketData(emptyColumn, emptyLightData);
    }

    /**
     * Extracts light data from the Column object created by ChunkProcessorFactory.
     * This method retrieves the proper lighting data when preserveOriginalLighting is enabled.
     *
     * @param column The column containing chunk data with lighting information
     * @param context The processing context
     * @return Light data extracted from the column
     * @throws ChunkProcessingException if light data extraction fails
     */
    private LightData extractLightDataFromColumn(Column column, ChunkProcessingContext context) throws ChunkProcessingException {
        try {
            // Use LightDataProcessor to create correct light data
            // Since the Column object is created by ChunkProcessorFactory, it already contains correct light processing
            // We need to extract light data from the original chunk snapshot
            
            int ySections = context.getPacketUser().getTotalWorldHeight() >> 4;
            org.bukkit.Chunk bukkitChunk = context.getPlayer().getWorld().getChunkAt(context.getChunk().x(), context.getChunk().z());
            org.bukkit.ChunkSnapshot chunkSnapshot = bukkitChunk.getChunkSnapshot();
            int maxHeight = context.getPlayer().getWorld().getMaxHeight();
            int minHeight = context.getPlayer().getWorld().getMinHeight();
            
            // Use LightDataProcessor to create complete light data
            dev.twme.blocket.processors.LightDataProcessor lightProcessor = new dev.twme.blocket.processors.LightDataProcessor();
            return lightProcessor.createLightData(chunkSnapshot, ySections, minHeight, maxHeight);
            
        } catch (Exception e) {
            // If light data extraction fails, fall back to empty light data to ensure backward compatibility
            api.getOwnerPlugin().getLogger().warning("Failed to extract light data from column, falling back to empty light data: " + e.getMessage());
            return createEmptyLightData(context);
        }
    }

    /**
     * Creates empty light data to let the client handle lighting.
     *
     * @param context The processing context
     * @return Empty light data
     */
    private LightData createEmptyLightData(ChunkProcessingContext context) {
        int ySections = context.getPacketUser().getTotalWorldHeight() >> 4;
        byte[][] emptyLightArray = new byte[ySections][2048];
        BitSet emptyBitSet = new BitSet(ySections);
        emptyBitSet.set(0, ySections);

        LightData lightData = new LightData();
        lightData.setBlockLightArray(emptyLightArray);
        lightData.setSkyLightArray(emptyLightArray);
        lightData.setBlockLightCount(ySections);
        lightData.setSkyLightCount(ySections);
        lightData.setBlockLightMask(new BitSet(ySections));
        lightData.setSkyLightMask(new BitSet(ySections));
        lightData.setEmptyBlockLightMask(emptyBitSet);
        lightData.setEmptySkyLightMask(emptyBitSet);
        return lightData;
    }

    /**
     * Sends chunk packets to the user.
     *
     * @param packetUser The packet user
     * @param chunk The chunk
     * @param packetData The packet data
     */
    private void sendChunkPackets(User packetUser, BlocketChunk chunk, ChunkPacketData packetData) {
        WrapperPlayServerUnloadChunk unloadPacket = new WrapperPlayServerUnloadChunk(chunk.x(), chunk.z());
        packetUser.sendPacketSilently(unloadPacket);
        WrapperPlayServerChunkData chunkDataPacket = new WrapperPlayServerChunkData(packetData.getColumn(), packetData.getLightData());
        packetUser.sendPacketSilently(chunkDataPacket);
    }

    /**
     * Handles chunk processing errors.
     *
     * @param player The player
     * @param chunk The chunk
     * @param e The exception
     */
    private void handleChunkProcessingError(Player player, BlocketChunk chunk, ChunkProcessingException e) {
        String errorMessage = String.format("Error processing chunk for player: %s, chunk: (%d, %d), error: %s", player.getName(), chunk.x(), chunk.z(), e.getMessage());
        api.getOwnerPlugin().getLogger().warning(errorMessage);
        if (e.getCause() != null) {
            api.getOwnerPlugin().getLogger().warning("Caused by: " + e.getCause().getMessage());
        }
    }

    /**
     * Handles unexpected errors during chunk processing.
     *
     * @param player The player
     * @param chunk The chunk
     * @param e The exception
     */
    private void handleUnexpectedError(Player player, BlocketChunk chunk, Exception e) {
        String errorMessage = String.format("Unexpected error processing chunk for player: %s, chunk: (%d, %d)", player.getName(), chunk.x(), chunk.z());
        api.getOwnerPlugin().getLogger().log(java.util.logging.Level.SEVERE, errorMessage, e);
    }

    /**
     * Gets the performance monitor.
     *
     * @return The performance monitor instance
     */
    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }

    /**
     * Gets the performance statistics report.
     *
     * @return The performance report string
     */
    public String getPerformanceReport() {
        return performanceMonitor.generateReport();
    }

    /**
     * Resets the performance statistics.
     */
    public void resetPerformanceStats() {
        performanceMonitor.resetAll();
    }

    /**
     * Shuts down and cleans up all resources.
     */
    public void shutdown() {
        blockChangeTasks.values().forEach(BukkitTask::cancel);
        blockChangeTasks.clear();

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("BlockChangeManager executor service did not terminate properly.");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        playerBlockChanges.clear();
        playerViewBlocks.clear();
        blockDataToId.clear();

        if (chunkProcessorFactory != null) {
            chunkProcessorFactory.clearCaches();
        }
        blockDataMapPool.clear();
        chunkListPool.clear();
        lightDataArrayPool.clear();

        if (api.getOwnerPlugin().getLogger() != null) {
            api.getOwnerPlugin().getLogger().info("BlockChangeManager Performance Stats:\n" + performanceMonitor.generateReport());
        }
    }
}
