package dev.twme.blocket.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import dev.twme.blocket.types.BlocketPosition;

/**
 * Utility class providing helper methods for block operations and calculations.
 * This class contains static methods for common block-related operations
 * such as region calculations, location conversions, and block data manipulation.
 * 
 * <p>The BlockUtils class provides:
 * <ul>
 *   <li>3D region block enumeration between two points</li>
 *   <li>Location list generation for cubic areas</li>
 *   <li>Block age manipulation for crops and similar blocks</li>
 *   <li>Efficient block position calculations</li>
 * </ul>
 * 
 * <p>Performance consideration: The block enumeration methods can generate
 * large amounts of data for big regions. It is recommended to call these
 * methods asynchronously when dealing with large areas to avoid blocking
 * the main server thread.</p>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlockUtils {

    /**
     * Get all the blocks between two positions.
     * Call this method asynchronously if you are going to be getting a large amount of blocks.
     *
     * @param pos1 The first position.
     * @param pos2 The second position.
     * @return A set of all the blocks between the two positions.
     */
    public static Set<BlocketPosition> getBlocksBetween(BlocketPosition pos1, BlocketPosition pos2) {
        Set<BlocketPosition> positions = new HashSet<>();
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    positions.add(new BlocketPosition(x, y, z));
                }
            }
        }
        return positions;
    }

    /**
     * Get all the locations between two locations.
     * Call this method asynchronously if you are going to be getting a large amount of locations.
     *
     * @param loc1 The first location.
     * @param loc2 The second location.
     * @return A list of all the locations between the two locations.
     */
    public static List<Location> getLocationsBetween(Location loc1, Location loc2) {
        List<Location> locations = new ArrayList<>();
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    locations.add(new Location(loc1.getWorld(), x, y, z));
                }
            }
        }
        return locations;
    }

    /**
     * Set the age of a block.
     *
     * @param blockData The block data.
     * @param age The age to set.
     * @return The block data with the age set.
     */
    public static BlockData setAge(BlockData blockData, int age) {
        Ageable ageable = (Ageable) blockData;
        ageable.setAge(age);
        return ageable;
    }

}