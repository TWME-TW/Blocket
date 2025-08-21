package dev.twme.blocket.models;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.types.BlocketChunk;
import dev.twme.blocket.types.BlocketPosition;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents a view layer within a stage containing virtual block patterns.
 * A view manages virtual blocks for a specific layer in a stage, allowing
 * for breakable/placeable configurations and pattern-based block generation.
 *
 * <p>Views are ordered by zIndex, with higher values rendering on top.
 * Each view can have its own pattern for generating blocks and can be
 * individually controlled for breakability and placeability.</p>
 *
 * @author TWME-TW
 * @version 1.0.1
 * @since 1.0.0
 */
@Getter
@Setter
public class View {
    private final Map<BlocketChunk, Map<BlocketPosition, BlockData>> blocks;
    private Stage stage;
    private final String name;
    private int zIndex;
    private boolean breakable, placeable;
    private Pattern pattern;

    /**
     * Creates a new View with the specified parameters.
     *
     * @param name The unique name for this view within the stage
     * @param stage The stage this view belongs to
     * @param pattern The pattern used for generating blocks in this view
     * @param breakable Whether blocks in this view can be broken by players
     */
    public View(@NonNull String name, @NonNull Stage stage, @NonNull Pattern pattern, boolean breakable) {
        this.name = name;
        this.blocks = new ConcurrentHashMap<>();
        this.stage = stage;
        this.breakable = breakable;
        this.pattern = pattern;
        this.zIndex = 0;
    }

    /**
     * Gets the highest solid block at the specified X and Z coordinates within this view.
     * Searches from the stage's maximum Y down to minimum Y.
     *
     * @param x The X coordinate to search
     * @param z The Z coordinate to search
     * @return The highest solid block position, or null if no solid block is found
     */
    public BlocketPosition getHighestBlock(int x, int z) {
        for (int y = stage.getMaxPosition().getY(); y >= stage.getMinPosition().getY(); y--) {
            BlocketPosition position = new BlocketPosition(x, y, z);
            if (hasBlock(position) && getBlock(position).getMaterial().isSolid()) {
                return position;
            }
        }
        return null;
    }

    /**
     * Gets the lowest solid block at the specified X and Z coordinates within this view.
     * Searches from the stage's minimum Y up to maximum Y.
     *
     * @param x The X coordinate to search
     * @param z The Z coordinate to search
     * @return The lowest solid block position, or null if no solid block is found
     */
    public BlocketPosition getLowestBlock(int x, int z) {
        for (int y = stage.getMinPosition().getY(); y <= stage.getMaxPosition().getY(); y++) {
            BlocketPosition position = new BlocketPosition(x, y, z);
            if (hasBlock(position) && getBlock(position).getMaterial().isSolid()) {
                return position;
            }
        }
        return null;
    }

    /**
     * Removes a block at the specified position from this view.
     * Updates the block change cache for all viewers in the stage's audience.
     *
     * @param position The position of the block to remove
     */
    public void removeBlock(@NonNull BlocketPosition position) {
        BlocketChunk chunk = position.toBlocketChunk();
        Map<BlocketPosition, BlockData> chunkMap = blocks.get(chunk);
        if (chunkMap != null && chunkMap.remove(position) != null && chunkMap.isEmpty()) {
            blocks.remove(chunk);
        }

        // Also update each viewer's cache: data = null means remove the block
        for (Player viewer : stage.getAudience().getOnlinePlayers()) {
            BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, null, this.name);
        }
    }

    /**
     * Removes multiple blocks at the specified positions from this view.
     * This is a convenience method that calls removeBlock for each position.
     *
     * @param positions A set of positions where blocks should be removed
     */
    public void removeBlocks(@NonNull Set<BlocketPosition> positions) {
        positions.forEach(this::removeBlock);
    }

    /**
     * Removes all blocks from this view and clears the view's block cache.
     * Updates the block change cache for all viewers to reflect the removal.
     */
    public void removeAllBlocks() {
        // Removing all blocks in bulk, update caches accordingly
        blocks.forEach((chunk, chunkMap) -> {
            if (chunkMap == null) return;
            chunkMap.keySet().forEach(position -> {
                // Apply removal to each viewer
                for (Player viewer : stage.getAudience().getOnlinePlayers()) {
                    BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, null, this.name);
                }
            });
        });
        blocks.clear();
    }

    /**
     * Adds a block at the specified position using a randomly generated block from this view's pattern.
     * Updates the block change cache for all viewers in the stage's audience.
     *
     * @param position The position where the block should be added
     */
    public void addBlock(@NonNull BlocketPosition position) {
        BlockData newData = pattern.getRandomBlockData();
        BlocketChunk chunk = position.toBlocketChunk();
        blocks.computeIfAbsent(chunk, c -> new ConcurrentHashMap<>()).put(position, newData);

        // Update each viewer's cache with the new block
        for (Player viewer : stage.getAudience().getOnlinePlayers()) {
            BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, newData, this.name);
        }
    }

    /**
     * Adds multiple blocks at the specified positions using randomly generated blocks from this view's pattern.
     * This is a convenience method that calls addBlock for each position.
     *
     * @param positions A set of positions where blocks should be added
     */
    public void addBlocks(@NonNull Set<BlocketPosition> positions) {
        positions.forEach(this::addBlock);
    }

    /**
     * Checks if a block exists at the specified position in this view.
     *
     * @param position The position to check
     * @return true if a block exists at the position, false otherwise
     */
    public boolean hasBlock(@NonNull BlocketPosition position) {
        Map<BlocketPosition, BlockData> chunkMap = blocks.get(position.toBlocketChunk());
        return chunkMap != null && chunkMap.containsKey(position);
    }

    /**
     * Checks if all blocks at the specified positions exist in this view.
     *
     * @param positions A set of positions to check
     * @return true if all positions have blocks, false if any position is missing a block
     */
    public boolean hasBlocks(@NonNull Set<BlocketPosition> positions) {
        return positions.stream().allMatch(this::hasBlock);
    }

    /**
     * Gets the block data at the specified position.
     *
     * @param position The position to get the block data from
     * @return The BlockData at the position, or null if no block exists
     */
    public BlockData getBlock(@NonNull BlocketPosition position) {
        Map<BlocketPosition, BlockData> chunkMap = blocks.get(position.toBlocketChunk());
        return (chunkMap == null) ? null : chunkMap.get(position);
    }

    /**
     * Checks if this view has blocks in the specified chunk coordinates.
     *
     * @param x The chunk X coordinate
     * @param z The chunk Z coordinate
     * @return true if the view contains blocks in the specified chunk
     */
    public boolean hasChunk(int x, int z) {
        return blocks.containsKey(new BlocketChunk(x, z));
    }

    /**
     * Sets multiple blocks at the specified positions to the same block data.
     * This is a convenience method that calls setBlock for each position.
     *
     * @param positions A set of positions where blocks should be set
     * @param blockData The block data to set at all positions
     */
    public void setBlocks(@NonNull Set<BlocketPosition> positions, @NonNull BlockData blockData) {
        positions.forEach(position -> setBlock(position, blockData));
    }

    /**
     * Sets a specific block at the specified position if a block already exists there.
     * Updates the block change cache for all viewers in the stage's audience.
     *
     * @param position The position of the block to set
     * @param blockData The block data to set
     */
    public void setBlock(@NonNull BlocketPosition position, @NonNull BlockData blockData) {
        if (hasBlock(position)) {
            BlocketChunk chunk = position.toBlocketChunk();
            blocks.get(chunk).put(position, blockData);

            // Update each viewer's cache with the updated block
            for (Player viewer : stage.getAudience().getOnlinePlayers()) {
                BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, blockData, this.name);
            }
        }
    }

    /**
     * Resets a block at the specified position to a new randomly generated block from this view's pattern.
     * Only works if a block already exists at the position.
     *
     * @param position The position of the block to reset
     */
    public void resetBlock(@NonNull BlocketPosition position) {
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

    /**
     * Resets multiple blocks at the specified positions to new randomly generated blocks from this view's pattern.
     * This is a convenience method that calls resetBlock for each position.
     *
     * @param positions A set of positions where blocks should be reset
     */
    public void resetBlocks(@NonNull Set<BlocketPosition> positions) {
        positions.forEach(this::resetBlock);
    }

    /**
     * Resets all blocks in this view to new randomly generated blocks from this view's pattern.
     * Updates the block change cache for all viewers to reflect the changes.
     */
    public void resetViewBlocks() {
        blocks.forEach((chunk, chunkMap) -> {
            chunkMap.keySet().forEach(position -> {
                BlockData newData = pattern.getRandomBlockData();
                chunkMap.put(position, newData);
                // Update viewers
                for (Player viewer : stage.getAudience().getOnlinePlayers()) {
                    BlocketAPI.getInstance().getBlockChangeManager().applyBlockChange(viewer, chunk, position, newData, this.name);
                }
            });
        });
    }

    /**
     * Changes the pattern used for generating blocks in this view.
     * This does not affect existing blocks, only future block generation.
     *
     * @param pattern The new pattern to use for block generation
     */
    public void changePattern(@NonNull Pattern pattern) {
        this.pattern = pattern;
    }
}
