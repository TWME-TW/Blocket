package dev.twme.blocket.types;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Represents a position in 3D space for Blocket virtual blocks.
 * This class provides utility methods for converting between different position formats
 * and working with Minecraft's coordinate system.
 */
public class BlocketPosition {
    private int x, y, z;

    /**
     * Create a new BlocketPosition
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     */
    public BlocketPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Create a new BlocketPosition
     *
     * @param location The location to create the BlocketPosition from
     * @return A new BlocketPosition based on the given location
     */
    public static BlocketPosition fromLocation(Location location) {
        return new BlocketPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Create a new BlocketPosition
     *
     * @param vector The vector to create the BlocketPosition from
     * @return A new BlocketPosition based on the given vector
     */
    public static BlocketPosition fromVector(Vector vector) {
        return new BlocketPosition(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    /**
     * Creates new BlocketPositions
     *
     * @param locations The locations to create the BlocketPosition from
     * @return A set of BlocketPositions based on the given locations
     */
    public static Set<BlocketPosition> fromLocations(Set<Location> locations) {
        return locations.stream().map(BlocketPosition::fromLocation).collect(Collectors.toSet());
    }

    /**
     * Creates new BlocketPositions
     *
     * @param blockPositions The block positions to create the BlocketPosition from
     * @return A set of BlocketPositions based on the given block positions
     */
    public static Set<BlocketPosition> fromPositions(Set<BlockPosition> blockPositions) {
        return blockPositions.stream().map(BlocketPosition::fromPosition).collect(Collectors.toSet());
    }

    /**
     * Create a new BlocketPosition
     *
     * @param position The position to create the BlocketPosition from
     * @return A new BlocketPosition based on the given position
     */
    public static BlocketPosition fromPosition(Position position) {
        return new BlocketPosition(position.blockX(), position.blockY(), position.blockZ());
    }

    /**
     * Converts the BlocketPosition to a BlockPosition
     *
     * @return The BlockPosition representation of the BlocketPosition
     */
    public BlockPosition toBlockPosition() {
        return new BlockPosition() {
            @Override
            public int blockX() { return x; }
            
            @Override
            public int blockY() { return y; }
            
            @Override
            public int blockZ() { return z; }
        };
    }

    /**
     * Converts the BlocketPosition to a BlocketChunk
     *
     * @return The BlocketChunk at the BlocketPosition.
     */
    public BlocketChunk toBlocketChunk() {
        return new BlocketChunk(x >> 4, z >> 4);
    }

    /**
     * Converts the BlocketPosition to a Location
     *
     * @param world The world to convert the BlocketPosition to
     * @return The Location representation of the BlocketPosition
     */
    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    /**
     * Converts the BlocketPosition to a Position
     *
     * @return The Position representation of the BlocketPosition
     */
    public Position toPosition() {
        return Position.block(x, y, z);
    }

    /**
     * Converts the BlocketPosition to a Vector
     *
     * @return The Vector representation of the BlocketPosition
     */
    public Vector toVector() {
        return new Vector(x, y, z);
    }

    /**
     * Get the distance squared between two BlocketPositions
     *
     * @param other The other BlocketPosition
     * @return The distance squared between the two BlocketPositions
     */
    public double distanceSquared(BlocketPosition other) {
        return Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2) + Math.pow(z - other.z, 2);
    }

    /**
     * Get the block state at the BlocketPosition
     *
     * @param world The world to get the block state from
     * @return The block state at the BlocketPosition
     */
    public BlockState getBlock(World world) {
        return world.getBlockAt(x, y, z).getState();
    }

    /**
     * Get the block data at the BlocketPosition
     *
     * @param world The world to get the block data from
     * @return The block data at the BlocketPosition
     */
    public BlockData getBlockData(World world) {
        return world.getBlockAt(x, y, z).getBlockData();
    }

    /**
     * Get the distance between two BlocketPositions
     *
     * @param other The other BlocketPosition
     * @return The distance between the two BlocketPositions
     */
    public double distance(BlocketPosition other) {
        return Math.sqrt(distanceSquared(other));
    }

    /**
     * Get the string representation of the BlocketPosition
     *
     * @return The string representation of the BlocketPosition
     */
    @Override
    public String toString() {
        return "BlocketPosition{x=" + x + ", y=" + y + ", z=" + z + "}";
    }

    /**
     * Check if the BlocketPosition is equal to another object
     *
     * @param o The object to compare to
     * @return Whether the BlocketPosition is equal to the object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!(o instanceof BlocketPosition other)) return false;
        return x == other.x && y == other.y && z == other.z;
    }

    /**
     * Get the hash code of the BlocketPosition
     *
     * @return The hash code of the BlocketPosition
     */
    @Override
    public int hashCode() {
        return (x * 31 + y) * 31 + z;
    }
}
