package dev.twme.blocket.models;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.types.BlocketChunk;
import dev.twme.blocket.types.BlocketPosition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class View {
    @Getter
    private ConcurrentHashMap<BlocketChunk, ConcurrentHashMap<BlocketPosition, BlockData>> blocks;
    private Stage stage;
    private String name;
    private int zIndex;
    private boolean breakable, placeable;
    private Pattern pattern;

    public View(String name, Stage stage, Pattern pattern, boolean breakable) {
        this.name = name;
        this.blocks = new ConcurrentHashMap<>();
        this.stage = stage;
        this.breakable = breakable;
        this.pattern = pattern;
        this.zIndex = 0;
    }

    public BlocketPosition getHighestBlock(int x, int z) {
        for (int y = stage.getMaxPosition().getY(); y >= stage.getMinPosition().getY(); y--) {
            BlocketPosition position = new BlocketPosition(x, y, z);
            if (hasBlock(position) && getBlock(position).getMaterial().isSolid()) {
                return position;
            }
        }
        return null;
    }

    public BlocketPosition getLowestBlock(int x, int z) {
        for (int y = stage.getMinPosition().getY(); y <= stage.getMaxPosition().getY(); y++) {
            BlocketPosition position = new BlocketPosition(x, y, z);
            if (hasBlock(position) && getBlock(position).getMaterial().isSolid()) {
                return position;
            }
        }
        return null;
    }

    public void removeBlock(BlocketPosition position) {
        BlocketChunk chunk = position.toBlocketChunk();
        ConcurrentHashMap<BlocketPosition, BlockData> chunkMap = blocks.get(chunk);
        if (chunkMap != null && chunkMap.remove(position) != null && chunkMap.isEmpty()) {
            blocks.remove(chunk);
        }

        // Also update each viewer's cache: data = null means remove the block
        for (Player viewer : stage.getAudience().getOnlinePlayers()) {
            BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, null, this.name);
        }
    }

    public void removeBlocks(Set<BlocketPosition> positions) {
        for (BlocketPosition position : positions) {
            removeBlock(position);
        }
    }

    public void removeAllBlocks() {
        // Removing all blocks in bulk, update caches accordingly
        for (BlocketChunk chunk : blocks.keySet()) {
            ConcurrentHashMap<BlocketPosition, BlockData> chunkMap = blocks.get(chunk);
            if (chunkMap == null) continue;
            for (BlocketPosition position : chunkMap.keySet()) {
                // Apply removal to each viewer
                for (Player viewer : stage.getAudience().getOnlinePlayers()) {
                    BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, null, this.name);
                }
            }
        }
        blocks.clear();
    }

    public void addBlock(BlocketPosition position) {
        BlockData newData = pattern.getRandomBlockData();
        BlocketChunk chunk = position.toBlocketChunk();
        blocks.computeIfAbsent(chunk, c -> new ConcurrentHashMap<>()).put(position, newData);

        // Update each viewer's cache with the new block
        for (Player viewer : stage.getAudience().getOnlinePlayers()) {
            BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, newData, this.name);
        }
    }

    public void addBlocks(Set<BlocketPosition> positions) {
        for (BlocketPosition position : positions) {
            addBlock(position);
        }
    }

    public boolean hasBlock(BlocketPosition position) {
        ConcurrentHashMap<BlocketPosition, BlockData> chunkMap = blocks.get(position.toBlocketChunk());
        return chunkMap != null && chunkMap.containsKey(position);
    }

    public boolean hasBlocks(Set<BlocketPosition> positions) {
        for (BlocketPosition position : positions) {
            if (!hasBlock(position)) {
                return false;
            }
        }
        return true;
    }

    public BlockData getBlock(BlocketPosition position) {
        ConcurrentHashMap<BlocketPosition, BlockData> chunkMap = blocks.get(position.toBlocketChunk());
        return (chunkMap == null) ? null : chunkMap.get(position);
    }

    public boolean hasChunk(int x, int z) {
        return blocks.containsKey(new BlocketChunk(x, z));
    }

    public void setBlocks(Set<BlocketPosition> positions, BlockData blockData) {
        for (BlocketPosition position : positions) {
            setBlock(position, blockData);
        }
    }

    public void setBlock(BlocketPosition position, BlockData blockData) {
        if (hasBlock(position)) {
            BlocketChunk chunk = position.toBlocketChunk();
            blocks.get(chunk).put(position, blockData);

            // Update each viewer's cache with the updated block
            for (Player viewer : stage.getAudience().getOnlinePlayers()) {
                BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, blockData, this.name);
            }
        }
    }

    public void resetBlock(BlocketPosition position) {
        if (hasBlock(position)) {
            BlockData newData = pattern.getRandomBlockData();
            BlocketChunk chunk = position.toBlocketChunk();
            blocks.get(chunk).put(position, newData);

            // Update viewers
            for (Player viewer : stage.getAudience().getOnlinePlayers()) {
                BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, newData, this.name);
            }
        }
    }

    public void resetBlocks(Set<BlocketPosition> positions) {
        for (BlocketPosition position : positions) {
            resetBlock(position);
        }
    }

    public void resetViewBlocks() {
        for (BlocketChunk chunk : blocks.keySet()) {
            ConcurrentHashMap<BlocketPosition, BlockData> chunkMap = blocks.get(chunk);
            for (BlocketPosition position : chunkMap.keySet()) {
                BlockData newData = pattern.getRandomBlockData();
                chunkMap.put(position, newData);
                // Update viewers
                for (Player viewer : stage.getAudience().getOnlinePlayers()) {
                    BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, newData, this.name);
                }
            }
        }
    }

    public void changePattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
