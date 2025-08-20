package dev.twme.blocket.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import dev.twme.blocket.api.BlocketAPI;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a collection of players that can view virtual blocks in stages.
 * An audience manages player membership, visibility settings, and individual
 * player mining speeds for enhanced block breaking experiences.
 * 
 * <p>The Audience class provides functionality for:
 * <ul>
 *   <li>Managing player membership with UUID-based tracking</li>
 *   <li>Controlling player visibility (hiding players from each other)</li>
 *   <li>Setting individual mining speeds for players</li>
 *   <li>Converting between Player objects and UUIDs</li>
 *   <li>Filtering online players from the audience</li>
 * </ul>
 * 
 * <p>Mining speeds allow for customized block breaking experiences where
 * different players can have different breaking speeds, enhancing gameplay
 * mechanics like mining boosts or penalties.</p>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
@Setter
@Getter
public class Audience {
    private boolean arePlayersHidden;
    private final Set<UUID> players;
    private final Map<UUID, Float> miningSpeeds;

    /**
     * Creates an Audience from a set of players with default visibility settings.
     * @param players The set of players to include in the audience
     * @return A new Audience containing the specified players
     */
    public static Audience fromPlayers(Set<Player> players) {
        return new Audience(players.stream().map(Player::getUniqueId).collect(Collectors.toSet()), false);
    }

    /**
     * Creates an Audience from a set of player UUIDs with default visibility settings.
     * @param players The set of player UUIDs to include in the audience
     * @return A new Audience containing the specified players
     */
    public static Audience fromUUIDs(Set<UUID> players) {
        return new Audience(players, false);
    }

    /**
     * Creates an Audience from a set of players with specified visibility settings.
     * @param players The set of players to include in the audience
     * @param arePlayersHidden Whether the players should be hidden from view changes
     * @return A new Audience containing the specified players with the given visibility settings
     */
    public static Audience fromPlayers(Set<Player> players, boolean arePlayersHidden) {
        return new Audience(players.stream().map(Player::getUniqueId).collect(Collectors.toSet()), arePlayersHidden);
    }

    /**
     * Creates an Audience from a set of player UUIDs with specified visibility settings.
     * @param players The set of player UUIDs to include in the audience
     * @param arePlayersHidden Whether the players should be hidden from view changes
     * @return A new Audience containing the specified players with the given visibility settings
     */
    public static Audience fromUUIDs(Set<UUID> players, boolean arePlayersHidden) {
        return new Audience(players, arePlayersHidden);
    }

    /**
     * @param players The set of players
     * @param arePlayersHidden Whether the players are hidden
     */
    private Audience(Set<UUID> players, boolean arePlayersHidden) {
        this.players = players;
        this.arePlayersHidden = arePlayersHidden;
        this.miningSpeeds = new HashMap<>();
    }

    /**
     * Adds a player to this audience and returns the updated set of player UUIDs.
     * @param player The player to add to the audience
     * @return The updated set of player UUIDs after adding the player
     */
    public Set<UUID> addPlayer(Player player) {
        return addPlayer(player.getUniqueId());
    }

    /**
     * Adds a player to this audience by UUID and returns the updated set of player UUIDs.
     * @param player The UUID of a player to add to the audience
     * @return The updated set of player UUIDs after adding the player
     */
    public Set<UUID> addPlayer(UUID player) {
        players.add(player);
        return players;
    }

    /**
     * Removes a player from this audience and returns the updated set of player UUIDs.
     * @param player The player to remove from the audience
     * @return The updated set of player UUIDs after removing the player
     */
    public Set<UUID> removePlayer(Player player) {
        return removePlayer(player.getUniqueId());
    }

    /**
     * Removes a player from this audience by UUID and returns the updated set of player UUIDs.
     * @param player The UUID of a player to remove from the audience
     * @return The updated set of player UUIDs after removing the player
     */
    public Set<UUID> removePlayer(UUID player) {
        players.remove(player);
        return players;
    }

    /**
     * Gets all online players that are part of this audience.
     * @return A set of online players in the audience
     */
    public Set<Player> getOnlinePlayers() {
        List<Player> onlinePlayers = new ArrayList<>();
        for (UUID player : players) {
            Player p = BlocketAPI.getInstance().getOwnerPlugin().getServer().getPlayer(player);
            if (p != null) {
                onlinePlayers.add(p);
            }
        }
        return new HashSet<>(onlinePlayers);
    }


    /**
     * Sets the mining speed for a player
     * @param player The player
     * @param speed The speed
     */
    public void setMiningSpeed(Player player, float speed) {
        setMiningSpeed(player.getUniqueId(), speed);
    }

    /**
     * Sets the mining speed for a player
     * @param player The player's UUID
     * @param speed The speed
     */
    public void setMiningSpeed(UUID player, float speed) {
        if (speed < 0 || speed == 1) {
            BlocketAPI.getInstance().getOwnerPlugin().getLogger().warning("Invalid mining speed for player " + player + ": " + speed);
            return;
        }
        miningSpeeds.put(player, speed);
    }

    /**
     * Resets the mining speed for a player
     * @param player The player
     */
    public void resetMiningSpeed(Player player) {
        resetMiningSpeed(player.getUniqueId());
    }

    /**
     * Resets the mining speed for a player
     * @param player The player's UUID
     */
    public void resetMiningSpeed(UUID player) {
        miningSpeeds.remove(player);
    }

    /**
     * Gets the mining speed of a player
     * @param player The player
     * @return The mining speed
     */
    public float getMiningSpeed(Player player) {
        return getMiningSpeed(player.getUniqueId());
    }

    /**
     * Gets the mining speed of a player
     * @param player The player's UUID
     * @return The mining speed
     */
    public float getMiningSpeed(UUID player) {
        return miningSpeeds.getOrDefault(player, 1f);
    }

}
