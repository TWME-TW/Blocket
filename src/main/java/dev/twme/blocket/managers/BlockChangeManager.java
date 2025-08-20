package dev.twme.blocket.managers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.LightData;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v_1_18.Chunk_v1_18;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.events.OnBlockChangeSendEvent;
import dev.twme.blocket.models.Audience;
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.View;
import dev.twme.blocket.types.BlocketChunk;
import dev.twme.blocket.types.BlocketPosition;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.papermc.paper.math.Position;
import lombok.Getter;

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
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class BlockChangeManager {
    private final BlocketAPI api;
    private final ConcurrentHashMap<UUID, BukkitTask> blockChangeTasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BlockData, Integer> blockDataToId = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // Per-player block changes: PlayerUUID -> (BlocketChunk -> (BlocketPosition -> BlockData))
    // This is updated incrementally as views are added/removed or blocks change.
    private final Map<UUID, Map<BlocketChunk, Map<BlocketPosition, BlockData>>> playerBlockChanges = new ConcurrentHashMap<>();

    // Track which blocks came from which view for each player:
    // PlayerUUID -> (ViewName -> (Chunk -> Positions))
    private final Map<UUID, Map<String, Map<BlocketChunk, Set<BlocketPosition>>>> playerViewBlocks = new ConcurrentHashMap<>();

    /**
     * Creates a new BlockChangeManager with the specified BlocketAPI instance.
     * Initializes thread pools and data structures for managing block changes.
     * 
     * @param api The BlocketAPI instance this manager belongs to
     */
    public BlockChangeManager(BlocketAPI api) {
        this.api = api;
    }

    /**
     * Initializes block change tracking for a new player.
     * Creates empty data structures for the player's block changes and view tracking.
     * 
     * @param player The player to initialize tracking for
     */
    public void initializePlayer(Player player) {
        playerBlockChanges.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        playerViewBlocks.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
    }

    /**
     * Removes all tracking data for a player when they disconnect or leave a stage.
     * Cleans up memory by removing the player's block change cache and view tracking.
     * 
     * @param player The player to remove tracking for
     */
    public void removePlayer(Player player) {
        playerBlockChanges.remove(player.getUniqueId());
        playerViewBlocks.remove(player.getUniqueId());
    }

    /**
     * Hide a view from a player.
     * This removes the view's blocks from the player's cache and then sends updated blocks so the player no longer sees them.
     * @param player The player to hide the view from
     * @param view The view to hide from the player
     */
    public void hideView(Player player, View view) {
        removeViewFromPlayer(player, view);
        view.getStage().sendBlocksToAudience();
    }

    /**
     * Add a view's blocks to a player's cache in place.
     * @param player The player to add the view to
     * @param view The view to add to the player's cache
     */
    public void addViewToPlayer(Player player, View view) {
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerCache = playerBlockChanges.get(player.getUniqueId());
        Map<String, Map<BlocketChunk, Set<BlocketPosition>>> viewMap = playerViewBlocks.get(player.getUniqueId());

        if (playerCache == null || viewMap == null) return;

        Map<BlocketChunk, Set<BlocketPosition>> viewBlockPositions = new HashMap<>();

        // Merge view blocks into player cache
        for (Map.Entry<BlocketChunk, ConcurrentHashMap<BlocketPosition, BlockData>> chunkEntry : view.getBlocks().entrySet()) {
            BlocketChunk chunk = chunkEntry.getKey();
            Map<BlocketPosition, BlockData> chunkMap = playerCache.computeIfAbsent(chunk, c -> new ConcurrentHashMap<>());

            for (Map.Entry<BlocketPosition, BlockData> posEntry : chunkEntry.getValue().entrySet()) {
                chunkMap.put(posEntry.getKey(), posEntry.getValue());
                viewBlockPositions.computeIfAbsent(chunk, c -> new HashSet<>()).add(posEntry.getKey());
            }
        }

        viewMap.put(view.getName(), viewBlockPositions);
    }

    /**
     * Remove a view's blocks from a player's cache in place.
     * @param player The player to remove the view from
     * @param view The view to remove from the player's cache
     */
    public void removeViewFromPlayer(Player player, View view) {
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerCache = playerBlockChanges.get(player.getUniqueId());
        Map<String, Map<BlocketChunk, Set<BlocketPosition>>> viewMap = playerViewBlocks.get(player.getUniqueId());

        if (playerCache == null || viewMap == null) return;

        Map<BlocketChunk, Set<BlocketPosition>> viewBlocks = viewMap.remove(view.getName());
        if (viewBlocks == null) return;

        // Remove only the blocks associated with this view
        for (Map.Entry<BlocketChunk, Set<BlocketPosition>> chunkEntry : viewBlocks.entrySet()) {
            Map<BlocketPosition, BlockData> chunkMap = playerCache.get(chunkEntry.getKey());
            if (chunkMap != null) {
                for (BlocketPosition pos : chunkEntry.getValue()) {
                    chunkMap.remove(pos);
                }
                if (chunkMap.isEmpty()) {
                    playerCache.remove(chunkEntry.getKey());
                }
            }
        }
    }

    /**
     * Apply a single block change for a player. If data is null, remove block.
     * @param player The player to apply the block change for
     * @param chunk The chunk containing the block
     * @param pos The position of the block
     * @param data The block data to apply, or null to remove the block
     * @param viewName The name of the view this block change belongs to
     */
    public void applyBlockChange(Player player, BlocketChunk chunk, BlocketPosition pos, BlockData data, String viewName) {
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerCache = playerBlockChanges.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        Map<BlocketPosition, BlockData> chunkMap = playerCache.computeIfAbsent(chunk, c -> new ConcurrentHashMap<>());
        Map<String, Map<BlocketChunk, Set<BlocketPosition>>> viewMap = playerViewBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        if (data == null) {
            // Remove block
            chunkMap.remove(pos);
            if (chunkMap.isEmpty()) {
                playerCache.remove(chunk);
            }

            // Also remove from the associated view if known
            if (viewName != null) {
                Map<BlocketChunk, Set<BlocketPosition>> viewChunks = viewMap.get(viewName);
                if (viewChunks != null) {
                    Set<BlocketPosition> positions = viewChunks.get(chunk);
                    if (positions != null) {
                        positions.remove(pos);
                        if (positions.isEmpty()) {
                            viewChunks.remove(chunk);
                            if (viewChunks.isEmpty()) {
                                viewMap.remove(viewName);
                            }
                        }
                    }
                }
            }
        } else {
            // Add or update block
            chunkMap.put(pos, data);

            if (viewName != null) {
                viewMap.computeIfAbsent(viewName, k -> new HashMap<>())
                        .computeIfAbsent(chunk, c -> new HashSet<>())
                        .add(pos);
            }
        }
    }

    /**
     * Retrieve block changes for a player filtered by requested chunks.
     */
    private Map<BlocketChunk, Map<BlocketPosition, BlockData>> getBlockChangesForPlayer(Player player, Collection<BlocketChunk> chunks) {
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> changes = playerBlockChanges.get(player.getUniqueId());
        if (changes == null || changes.isEmpty()) {
            return Collections.emptyMap();
        }

        if (chunks.isEmpty()) return Collections.emptyMap();

        Map<BlocketChunk, Map<BlocketPosition, BlockData>> result = new HashMap<>();
        for (BlocketChunk chunk : chunks) {
            Map<BlocketPosition, BlockData> data = changes.get(chunk);
            if (data != null && !data.isEmpty()) {
                result.put(chunk, data);
            }
        }
        return result;
    }

    /**
     * Sends block changes for a stage to an audience without unloading.
     * @param stage The stage containing the blocks to send
     * @param audience The audience to send the block changes to
     * @param chunks The chunks containing the blocks to send
     */
    public void sendBlockChanges(Stage stage, Audience audience, Collection<BlocketChunk> chunks) {
        sendBlockChanges(stage, audience, chunks, false);
    }

    /**
     * Sends block changes for a stage to an audience with optional unloading.
     * @param stage The stage containing the blocks to send
     * @param audience The audience to send the block changes to
     * @param chunks The chunks containing the blocks to send
     * @param unload Whether to unload the chunks after sending
     */
    public void sendBlockChanges(Stage stage, Audience audience, Collection<BlocketChunk> chunks, boolean unload) {
        for (Player player : audience.getOnlinePlayers()) {
            if (!player.isOnline() || player.getWorld() != stage.getWorld()) continue;

            Map<BlocketChunk, Map<BlocketPosition, BlockData>> blockChanges = getBlockChangesForPlayer(player, chunks);
            Bukkit.getScheduler().runTask(api.getOwnerPlugin(), () -> new OnBlockChangeSendEvent(stage, blockChanges).callEvent());

            AtomicInteger chunkIndex = new AtomicInteger(0);
            List<BlocketChunk> chunkList = new ArrayList<>(chunks);

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(api.getOwnerPlugin(), () -> {
                if (chunkIndex.get() >= chunkList.size()) {
                    cancelTask(player.getUniqueId());
                    return;
                }
                for (int i = 0; i < stage.getChunksPerTick() && chunkIndex.get() < chunkList.size(); i++) {
                    sendChunkPacket(player, chunkList.get(chunkIndex.getAndIncrement()), unload);
                }
            }, 0L, 1L);

            blockChangeTasks.put(player.getUniqueId(), task);
        }
    }

    /**
     * Sends multiple block changes to a single player.
     * @param player The player to send block changes to
     * @param blocks The set of block positions to send changes for
     */
    public void sendMultiBlockChange(Player player, Set<BlocketPosition> blocks) {
        final Map<Position, BlockData> blocksToSend = new HashMap<>();
        for (BlocketPosition position : blocks) {
            BlockData blockData = playerBlockChanges.get(player.getUniqueId()).get(position.toBlocketChunk()).get(position);
            if (blockData == null) continue;
            blocksToSend.put(position.toPosition(), blockData);
        }
        player.sendMultiBlockChange(blocksToSend);
    }

    private void cancelTask(UUID playerId) {
        Optional.ofNullable(blockChangeTasks.remove(playerId)).ifPresent(BukkitTask::cancel);
    }

    public void sendChunkPacket(Player player, BlocketChunk chunk, boolean unload) {
        executorService.submit(() -> processAndSendChunk(player, chunk, unload));
    }

    private void processAndSendChunk(Player player, BlocketChunk chunk, boolean unload) {
        try {
            User packetUser = PacketEvents.getAPI().getPlayerManager().getUser(player);
            int ySections = packetUser.getTotalWorldHeight() >> 4;
            Map<BlocketPosition, BlockData> blockData = null;

            if (!unload) {
                blockData = getBlockChangesForPlayer(player, Collections.singleton(chunk)).get(chunk);
            }

            Map<BlockData, WrappedBlockState> blockDataToState = new HashMap<>();
            List<BaseChunk> chunks = new ArrayList<>(ySections);
            Chunk bukkitChunk = player.getWorld().getChunkAt(chunk.x(), chunk.z());
            ChunkSnapshot chunkSnapshot = bukkitChunk.getChunkSnapshot();
            int maxHeight = player.getWorld().getMaxHeight();
            int minHeight = player.getWorld().getMinHeight();

            BlockData[][][][] defaultBlockData = new BlockData[ySections][16][16][16];
            for (int section = 0; section < ySections; section++) {
                int baseY = (section << 4) + minHeight;
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        int worldY = baseY + y;
                        if (worldY >= minHeight && worldY < maxHeight) {
                            for (int z = 0; z < 16; z++) {
                                defaultBlockData[section][x][y][z] = chunkSnapshot.getBlockData(x, worldY, z);
                            }
                        }
                    }
                }
            }

            // Use empty light data to let the client calculate lighting naturally
            byte[][] emptyLightArray = new byte[ySections][];
            BitSet emptyBitSet = new BitSet(ySections);
            for (int i = 0; i < ySections; i++) {
                emptyLightArray[i] = new byte[2048]; // All zeros, no light data provided
                emptyBitSet.set(i); // Mark as empty to let client handle lighting
            }
            BitSet fullBitSet = new BitSet(ySections); // Empty bit set

            for (int section = 0; section < ySections; section++) {
                Chunk_v1_18 baseChunk = new Chunk_v1_18();

                long baseY = (section << 4) + minHeight;
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        long worldY = baseY + y;
                        if (worldY >= minHeight && worldY < maxHeight) {
                            for (int z = 0; z < 16; z++) {
                                BlockData data = null;
                                BlocketPosition position = new BlocketPosition(x + (chunk.x() << 4),
                                        (section << 4) + y + minHeight, z + (chunk.z() << 4));

                                if (!unload && blockData != null) {
                                    data = blockData.get(position);
                                }

                                if (data == null) {
                                    data = defaultBlockData[section][x][y][z];
                                }

                                WrappedBlockState state = blockDataToState.computeIfAbsent(data, SpigotConversionUtil::fromBukkitBlockData);
                                baseChunk.set(x, y, z, state);
                            }
                        }
                    }
                }

                int biomeId = baseChunk.getBiomeData().palette.stateToId(1);
                int storageSize = baseChunk.getBiomeData().storage.getData().length;
                for (int index = 0; index < storageSize; index++) {
                    baseChunk.getBiomeData().storage.set(index, biomeId);
                }

                chunks.add(baseChunk);
            }

            // Create light data arrays
            byte[][] blockLightArray = new byte[ySections][];
            byte[][] skyLightArray = new byte[ySections][];
            
            // Initialize light data arrays
            for (int i = 0; i < ySections; i++) {
                blockLightArray[i] = new byte[2048];
                skyLightArray[i] = new byte[2048];
            }
            
            // Populate light data from chunk snapshot
            for (int section = 0; section < ySections; section++) {
                int baseY = (section << 4) + minHeight;
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        int worldY = baseY + y;
                        if (worldY >= minHeight && worldY < maxHeight) {
                            for (int z = 0; z < 16; z++) {
                                int blockLight = chunkSnapshot.getBlockEmittedLight(x, worldY, z);
                                int skyLight = chunkSnapshot.getBlockSkyLight(x, worldY, z);
                                
                                // Pack light data into the array
                                int index = y << 8 | z << 4 | x;
                                int byteIndex = index >> 1;
                                int nibbleIndex = index & 1;
                                
                                if (nibbleIndex == 0) {
                                    blockLightArray[section][byteIndex] = (byte) ((blockLightArray[section][byteIndex] & 0xF0) | (blockLight & 0xF));
                                    skyLightArray[section][byteIndex] = (byte) ((skyLightArray[section][byteIndex] & 0xF0) | (skyLight & 0xF));
                                } else {
                                    blockLightArray[section][byteIndex] = (byte) ((blockLightArray[section][byteIndex] & 0x0F) | ((blockLight & 0xF) << 4));
                                    skyLightArray[section][byteIndex] = (byte) ((skyLightArray[section][byteIndex] & 0x0F) | ((skyLight & 0xF) << 4));
                                }
                            }
                        }
                    }
                }
            }
            
            LightData lightData = new LightData();
            lightData.setBlockLightArray(blockLightArray);
            lightData.setSkyLightArray(skyLightArray);
            lightData.setBlockLightCount(ySections);
            lightData.setSkyLightCount(ySections);
            
            // Set light masks
            BitSet blockLightMask = new BitSet(ySections);
            BitSet skyLightMask = new BitSet(ySections);
            for (int i = 0; i < ySections; i++) {
                blockLightMask.set(i);
                skyLightMask.set(i);
            }
            lightData.setBlockLightMask(blockLightMask);
            lightData.setSkyLightMask(skyLightMask);
            lightData.setEmptyBlockLightMask(new BitSet(ySections)); // No empty sections
            lightData.setEmptySkyLightMask(new BitSet(ySections)); // No empty sections

            Column column = new Column(chunk.x(), chunk.z(), true, chunks.toArray(BaseChunk[]::new), null);
            WrapperPlayServerUnloadChunk wrapperPlayServerUnloadChunk = new WrapperPlayServerUnloadChunk(chunk.x(), chunk.z());
            packetUser.sendPacketSilently(wrapperPlayServerUnloadChunk);
            WrapperPlayServerChunkData chunkData = new WrapperPlayServerChunkData(column, lightData);
            packetUser.sendPacketSilently(chunkData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor service did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
