package dev.twme.blocket.models;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.types.BlocketChunk;
import dev.twme.blocket.types.BlocketPosition;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a bounded stage area that contains multiple views and manages an audience of players.
 * A stage defines a 3D region in a world where virtual blocks can be displayed to players.
 * It manages multiple views (layers) and handles audience permissions and chunk processing.
 * 
 * <p>Stages are the main container for organizing virtual blocks. They define boundaries,
 * manage views with different z-indexes, and control how block changes are sent to players.
 * Each stage has its own audience that determines which players can see the virtual blocks.</p>
 * 
 * @author TWME-TW  
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
public class Stage {
    private final String name;
    private final World world;
    private BlocketPosition maxPosition, minPosition;
    private final Set<View> views;
    private int chunksPerTick;
    private final Audience audience;

    /**
     * Creates a new stage with the specified parameters.
     * The stage boundaries are automatically calculated from the two positions.
     * 
     * @param name The unique name for this stage
     * @param world The world this stage exists in
     * @param pos1 The first corner position of the stage boundary
     * @param pos2 The second corner position of the stage boundary  
     * @param audience The audience that can see blocks in this stage
     */
    public Stage(String name, World world, BlocketPosition pos1, BlocketPosition pos2, Audience audience) {
        this.name = name;
        this.world = world;
        this.maxPosition = new BlocketPosition(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
        this.minPosition = new BlocketPosition(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
        this.views = new HashSet<>();
        this.audience = audience;
        this.chunksPerTick = 1;
    }

    /**
     * Checks if the specified location is within this stage's boundaries.
     * 
     * @param location The location to check
     * @return true if the location is within the stage boundaries, false otherwise
     */
    public boolean isLocationWithin(Location location) {
        return location.getWorld().equals(world)
                && location.getBlockX() >= minPosition.getX() && location.getBlockX() <= maxPosition.getX()
                && location.getBlockY() >= minPosition.getY() && location.getBlockY() <= maxPosition.getY()
                && location.getBlockZ() >= minPosition.getZ() && location.getBlockZ() <= maxPosition.getZ();
    }

    /**
     * Sends all blocks (from all views) to the audience.
     * Call this after you've done incremental updates (e.g., added/removed views for players).
     */
    public void sendBlocksToAudience() {
        BlocketAPI.getInstance().getBlockChangeManager().sendBlockChanges(this, audience, getChunks(), false);
    }

    /**
     * Refreshes a specific set of blocks to the audience.
     * Use this after making incremental block-level changes.
     * 
     * @param blocks The set of block positions to refresh
     */
    public void refreshBlocksToAudience(Set<BlocketPosition> blocks) {
        for (Player player : audience.getOnlinePlayers()) {
            BlocketAPI.getInstance().getBlockChangeManager().sendMultiBlockChange(player, blocks);
        }
    }

    /**
     * Adds a view to this stage if no view with the same name already exists.
     * Logs a warning if a view with the same name is already present.
     * 
     * @param view The view to add to this stage
     */
    public void addView(View view) {
        if (views.stream().anyMatch(v -> v.getName().equalsIgnoreCase(view.getName()))) {
            BlocketAPI.getInstance().getOwnerPlugin().getLogger().warning("View with name " + view.getName() + " already exists in stage " + name + "!");
            return;
        }
        views.add(view);
    }

    /**
     * Removes a view from this stage.
     * 
     * @param view The view to remove from this stage
     */
    public void removeView(View view) {
        views.remove(view);
    }

    /**
     * Gets a view by its name (case-insensitive).
     * 
     * @param viewName The name of the view to retrieve
     * @return The view with the specified name, or null if not found
     */
    public View getView(String viewName) {
        for (View view : views) {
            if (view.getName().equalsIgnoreCase(viewName)) {
                return view;
            }
        }
        return null;
    }

    /**
     * Gets all chunks that intersect with this stage's boundaries.
     * 
     * @return A set of all BlocketChunk objects within the stage boundaries
     */
    public Set<BlocketChunk> getChunks() {
        Set<BlocketChunk> chunks = new HashSet<>();
        for (int x = minPosition.getX() >> 4; x <= maxPosition.getX() >> 4; x++) {
            for (int z = minPosition.getZ() >> 4; z <= maxPosition.getZ() >> 4; z++) {
                chunks.add(new BlocketChunk(x, z));
            }
        }
        return chunks;
    }

    /**
     * Add a given view to a player. Uses existing methods:
     * - Stage: getView(...)
     * - View: getBlocks() (already present in View)
     * - BlockChangeManager: addViewToPlayer(player, view)
     * 
     * @param player The player to add the view for
     * @param view The view to add
     */
    public void addViewForPlayer(Player player, View view) {
        // This method uses BlockChangeManager's addViewToPlayer to merge the view's blocks into player's cache
        BlocketAPI.getInstance().getBlockChangeManager().addViewToPlayer(player, view);
        // After updating what the player sees, refresh all blocks
        sendBlocksToAudience();
    }

    /**
     * Add a view to a player by name.
     * 
     * @param player The player to add the view for
     * @param viewName The name of the view to add
     */
    public void addViewForPlayer(Player player, String viewName) {
        View view = getView(viewName);
        if (view == null) {
            player.sendMessage("View not found: " + viewName);
            return;
        }
        addViewForPlayer(player, view);
    }

    /**
     * Remove a given view from a player. Uses existing methods:
     * - View: getBlocks()
     * - BlockChangeManager: removeViewFromPlayer(player, view)
     * 
     * @param player The player to remove the view from
     * @param view The view to remove
     */
    public void removeViewForPlayer(Player player, View view) {
        // Remove view's blocks from player's cache
        BlocketAPI.getInstance().getBlockChangeManager().removeViewFromPlayer(player, view);
        // Refresh all blocks for audience after removing the view
        sendBlocksToAudience();
    }

    /**
     * Remove a view from a player by name.
     * 
     * @param player The player to remove the view from
     * @param viewName The name of the view to remove
     */
    public void removeViewForPlayer(Player player, String viewName) {
        View view = getView(viewName);
        if (view == null) {
            player.sendMessage("View not found: " + viewName);
            return;
        }
        removeViewForPlayer(player, view);
    }
}
