package dev.twme.blocket.managers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
    // Use a fixed-size thread pool to limit the number of concurrent tasks
    // Set as daemon threads to ensure they terminate correctly when the plugin is disabled
    private final ExecutorService executorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors() * 2,
        r -> {
            Thread t = new Thread(r, "Blocket-BlockChange-Worker");
            t.setDaemon(true);
            return t;
        }
    );

    // Per-player block changes: PlayerUUID -> (BlocketChunk -> (BlocketPosition -> BlockData))
    // This is updated incrementally as views are added/removed or blocks change.
    // Using ConcurrentHashMap for thread-safe operations
    private final Map<UUID, Map<BlocketChunk, Map<BlocketPosition, BlockData>>> playerBlockChanges = new ConcurrentHashMap<>();

    // Track which blocks came from which view for each player:
    // PlayerUUID -> (ViewName -> (Chunk -> Positions))
    // Using ConcurrentHashMap for thread-safe operations
    private final Map<UUID, Map<String, Map<BlocketChunk, Set<BlocketPosition>>>> playerViewBlocks = new ConcurrentHashMap<>();
    
    // LRU cache for block data
    private final LRUCache<BlocketChunk, Map<BlocketPosition, BlockData>> blockDataCache;
    
    // 區塊處理器工廠，用於創建和處理區塊數據
    private ChunkProcessorFactory chunkProcessorFactory;
    
    // 對象池，用於重用昂貴的對象
    private final ObjectPool<Map<BlocketPosition, BlockData>> blockDataMapPool;
    private final ObjectPool<List<BaseChunk>> chunkListPool;
    private final ObjectPool<byte[]> lightDataArrayPool;
    
    // 性能監控器
    private final PerformanceMonitor performanceMonitor;

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
        
        // 初始化對象池
        this.blockDataMapPool = new ObjectPool<>(HashMap::new, 50);
        this.chunkListPool = new ObjectPool<>(ArrayList::new, 20);
        this.lightDataArrayPool = new ObjectPool<>(() -> new byte[2048], 100);
        
        // 初始化性能監控器
        this.performanceMonitor = new PerformanceMonitor();
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
        // Clear player's data from cache
        Map<BlocketChunk, Map<BlocketPosition, BlockData>> playerChanges = playerBlockChanges.get(player.getUniqueId());
        if (playerChanges != null) {
            for (BlocketChunk chunk : playerChanges.keySet()) {
                blockDataCache.remove(chunk);
            }
        }
        
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

        Map<BlocketChunk, Set<BlocketPosition>> viewBlockPositions = new ConcurrentHashMap<>();

        // Merge view blocks into player cache
        for (Map.Entry<BlocketChunk, ConcurrentHashMap<BlocketPosition, BlockData>> chunkEntry : view.getBlocks().entrySet()) {
            BlocketChunk chunk = chunkEntry.getKey();
            Map<BlocketPosition, BlockData> chunkMap = playerCache.computeIfAbsent(chunk, c -> new ConcurrentHashMap<>());

            for (Map.Entry<BlocketPosition, BlockData> posEntry : chunkEntry.getValue().entrySet()) {
                chunkMap.put(posEntry.getKey(), posEntry.getValue());
                viewBlockPositions.computeIfAbsent(chunk, c -> ConcurrentHashMap.newKeySet()).add(posEntry.getKey());
            }
            
            // Remove chunk from cache as it will be updated
            blockDataCache.remove(chunk);
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
            
            // Remove chunk from cache as it will be updated
            blockDataCache.remove(chunkEntry.getKey());
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
        Map<String, Map<BlocketChunk, Set<BlocketPosition>>> viewMap = playerViewBlocks.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());

        // Remove chunk from cache as it will be updated
        blockDataCache.remove(chunk);

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
                viewMap.computeIfAbsent(viewName, k -> new ConcurrentHashMap<>())
                        .computeIfAbsent(chunk, c -> ConcurrentHashMap.newKeySet())
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
            // Try to get data from cache first
            Map<BlocketPosition, BlockData> data = blockDataCache.get(chunk);
            if (data == null) {
                // If not in cache, get from player changes and put in cache
                data = changes.get(chunk);
                if (data != null && !data.isEmpty()) {
                    blockDataCache.put(chunk, data);
                }
            }
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

    /**
     * 處理並發送區塊數據包
     * 重構後的主方法，將複雜邏輯拆分為多個小方法
     *
     * @param player 目標玩家
     * @param chunk 要處理的區塊
     * @param unload 是否為卸載操作
     */
    private void processAndSendChunk(Player player, BlocketChunk chunk, boolean unload) {
        try (PerformanceMonitor.Timer timer = performanceMonitor.startTimer("processAndSendChunk")) {
            // 驗證輸入參數
            validateChunkProcessingInputs(player, chunk);
            
            // 獲取基本信息
            ChunkProcessingContext context = createProcessingContext(player, chunk, unload);
            
            // 創建區塊數據包
            ChunkPacketData packetData = createChunkPacketData(context);
            
            // 發送數據包
            sendChunkPackets(context.getPacketUser(), chunk, packetData);
            
        } catch (ChunkProcessingException e) {
            performanceMonitor.incrementCounter("chunkProcessingErrors");
            handleChunkProcessingError(player, chunk, e);
        } catch (Exception e) {
            performanceMonitor.incrementCounter("unexpectedErrors");
            handleUnexpectedError(player, chunk, e);
        }
    }
    
    /**
     * 驗證區塊處理的輸入參數
     *
     * @param player 玩家
     * @param chunk 區塊
     * @throws ChunkProcessingException 當參數無效時拋出
     */
    private void validateChunkProcessingInputs(Player player, BlocketChunk chunk) throws ChunkProcessingException {
        if (player == null) {
            throw new ChunkProcessingException("玩家不能為null");
        }
        if (chunk == null) {
            throw new ChunkProcessingException("區塊不能為null");
        }
        if (!player.isOnline()) {
            throw new ChunkProcessingException("玩家必須在線上");
        }
        if (player.getWorld() == null) {
            throw new ChunkProcessingException("玩家世界不能為null");
        }
    }
    
    /**
     * 創建區塊處理上下文
     *
     * @param player 玩家
     * @param chunk 區塊
     * @param unload 是否為卸載操作
     * @return 處理上下文
     * @throws ChunkProcessingException 當創建上下文失敗時拋出
     */
    private ChunkProcessingContext createProcessingContext(Player player, BlocketChunk chunk, boolean unload)
            throws ChunkProcessingException {
        try {
            User packetUser = PacketEvents.getAPI().getPlayerManager().getUser(player);
            if (packetUser == null) {
                throw new ChunkProcessingException("無法獲取玩家的PacketUser");
            }
            
            Map<BlocketPosition, BlockData> customBlockData = null;
            if (!unload) {
                customBlockData = getBlockChangesForPlayer(player, Collections.singleton(chunk)).get(chunk);
            }
            
            return new ChunkProcessingContext(player, chunk, packetUser, customBlockData, unload);
            
        } catch (Exception e) {
            throw new ChunkProcessingException("創建處理上下文時發生錯誤", e);
        }
    }
    
    /**
     * 創建區塊數據包數據
     *
     * @param context 處理上下文
     * @return 區塊數據包數據
     * @throws ChunkProcessingException 當創建數據包失敗時拋出
     */
    private ChunkPacketData createChunkPacketData(ChunkProcessingContext context) throws ChunkProcessingException {
        try (PerformanceMonitor.Timer timer = performanceMonitor.startTimer("createChunkPacketData")) {
            // 對於卸載操作，創建簡單的空區塊數據
            if (context.isUnload()) {
                performanceMonitor.incrementCounter("emptyChunkPackets");
                return createEmptyChunkPacketData(context);
            }
            
            // 創建處理選項
            ChunkProcessorFactory.ChunkProcessingOptions options =
                new ChunkProcessorFactory.ChunkProcessingOptions(context.getPacketUser())
                    .useEmptyLighting(true); // 使用空光照讓客戶端自行計算
            
            // 創建區塊Column
            Column column = chunkProcessorFactory.createChunkColumn(
                context.getPlayer(),
                context.getChunk(),
                context.getCustomBlockData(),
                options
            );
            
            // 創建光照數據（空光照）
            LightData lightData = createEmptyLightData(context);
            
            performanceMonitor.incrementCounter("fullChunkPackets");
            return new ChunkPacketData(column, lightData);
            
        } catch (Exception e) {
            throw new ChunkProcessingException("創建區塊數據包數據時發生錯誤", e);
        }
    }
    
    /**
     * 創建空的區塊數據包數據（用於卸載操作）
     *
     * @param context 處理上下文
     * @return 空的區塊數據包數據
     */
    private ChunkPacketData createEmptyChunkPacketData(ChunkProcessingContext context) {
        // 創建空的Column和LightData
        BaseChunk[] emptyChunks = new BaseChunk[0];
        Column emptyColumn = new Column(context.getChunk().x(), context.getChunk().z(), true, emptyChunks, null);
        LightData emptyLightData = createEmptyLightData(context);
        
        return new ChunkPacketData(emptyColumn, emptyLightData);
    }
    
    /**
     * 創建空光照數據（讓客戶端自行計算光照）
     *
     * @param context 處理上下文
     * @return 空光照數據
     */
    private LightData createEmptyLightData(ChunkProcessingContext context) {
        int ySections = context.getPacketUser().getTotalWorldHeight() >> 4;
        
        // 創建空光照陣列
        byte[][] emptyLightArray = new byte[ySections][];
        for (int i = 0; i < ySections; i++) {
            emptyLightArray[i] = new byte[2048]; // 全部為0
        }
        
        // 創建空遮罩
        BitSet emptyBitSet = new BitSet(ySections);
        for (int i = 0; i < ySections; i++) {
            emptyBitSet.set(i); // 標記為空，讓客戶端處理光照
        }
        
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
     * 發送區塊數據包
     *
     * @param packetUser 數據包用戶
     * @param chunk 區塊
     * @param packetData 數據包數據
     */
    private void sendChunkPackets(User packetUser, BlocketChunk chunk, ChunkPacketData packetData) {
        // 先發送卸載數據包
        WrapperPlayServerUnloadChunk unloadPacket = new WrapperPlayServerUnloadChunk(chunk.x(), chunk.z());
        packetUser.sendPacketSilently(unloadPacket);
        
        // 再發送區塊數據包
        WrapperPlayServerChunkData chunkDataPacket = new WrapperPlayServerChunkData(
            packetData.getColumn(),
            packetData.getLightData()
        );
        packetUser.sendPacketSilently(chunkDataPacket);
    }
    
    /**
     * 處理區塊處理異常
     *
     * @param player 玩家
     * @param chunk 區塊
     * @param e 異常
     */
    private void handleChunkProcessingError(Player player, BlocketChunk chunk, ChunkProcessingException e) {
        String errorMessage = String.format(
            "處理區塊時發生錯誤 - 玩家: %s, 區塊: (%d, %d), 錯誤: %s",
            player.getName(), chunk.x(), chunk.z(), e.getMessage()
        );
        
        // 記錄詳細錯誤信息
        api.getOwnerPlugin().getLogger().warning(errorMessage);
        if (e.getCause() != null) {
            api.getOwnerPlugin().getLogger().warning("原因: " + e.getCause().getMessage());
        }
        
        // 可以選擇通知玩家或進行其他恢復操作
        // player.sendMessage("區塊載入時發生錯誤，請稍後再試");
    }
    
    /**
     * 處理意外錯誤
     *
     * @param player 玩家
     * @param chunk 區塊
     * @param e 異常
     */
    private void handleUnexpectedError(Player player, BlocketChunk chunk, Exception e) {
        String errorMessage = String.format(
            "處理區塊時發生意外錯誤 - 玩家: %s, 區塊: (%d, %d)",
            player.getName(), chunk.x(), chunk.z()
        );
        
        api.getOwnerPlugin().getLogger().severe(errorMessage);
        e.printStackTrace();
    }
    
    /**
     * 獲取性能監控器
     *
     * @return 性能監控器實例
     */
    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }
    
    /**
     * 獲取性能統計報告
     *
     * @return 性能統計報告字符串
     */
    public String getPerformanceReport() {
        return performanceMonitor.generateReport();
    }
    
    /**
     * 重置性能統計
     */
    public void resetPerformanceStats() {
        performanceMonitor.resetAll();
    }

    /**
     * 關閉並清理所有資源
     * 正確關閉 ExecutorService 並等待所有任務完成
     * 添加了更完善的錯誤處理和資源清理機制
     */
    public void shutdown() {
        // 取消所有正在進行的任務
        blockChangeTasks.values().forEach(BukkitTask::cancel);
        blockChangeTasks.clear();
        
        // 關閉 ExecutorService
        executorService.shutdown();
        try {
            // 等待最多 30 秒讓現有任務完成
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                // 如果還有任務未完成，強制關閉
                executorService.shutdownNow();
                // 再等待最多 10 秒確保關閉
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("BlockChangeManager executor service did not terminate properly");
                }
            }
        } catch (InterruptedException e) {
            // 如果等待過程中被中斷，強制關閉
            executorService.shutdownNow();
            Thread.currentThread().interrupt(); // 保持中斷狀態
        }
        
        // 清理其他資源
        playerBlockChanges.clear();
        playerViewBlocks.clear();
        blockDataToId.clear();
        
        // 清理對象池
        if (chunkProcessorFactory != null) {
            chunkProcessorFactory.clearCaches();
        }
        blockDataMapPool.clear();
        chunkListPool.clear();
        lightDataArrayPool.clear();
        
        // 輸出性能報告
        if (api.getOwnerPlugin().getLogger() != null) {
            api.getOwnerPlugin().getLogger().info("BlockChangeManager 性能統計:\n" + performanceMonitor.generateReport());
        }
    }
}
