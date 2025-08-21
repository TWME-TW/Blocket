package dev.twme.blocket.protocol;

import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.events.BlocketBreakEvent;
import dev.twme.blocket.events.BlocketInteractEvent;
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.types.BlocketPosition;

/**
 * Packet adapter that handles block digging interactions for virtual blocks.
 * This adapter intercepts player block digging packets and processes them
 * for blocks that exist in Blocket stages and views.
 * 
 * <p>The adapter handles:
 * <ul>
 *   <li>Block interaction events (left-click, start digging)</li>
 *   <li>Block breaking logic with breakability checks</li>
 *   <li>Instant break calculations based on tool efficiency and haste effects</li>
 *   <li>Creative mode handling</li>
 *   <li>Event cancellation for non-breakable blocks</li>
 * </ul>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
/**
 * Packet adapter for handling block digging interactions in Blocket.
 * This class intercepts player digging packets and processes them for virtual blocks.
 */
public class BlockDigAdapter extends SimplePacketListenerAbstract {

    /**
     * Handles incoming player digging packets and processes them for virtual blocks.
     * This method intercepts PLAYER_DIGGING packets and checks if the target block
     * exists in any of the player's active stages.
     * 
     * @param event The packet event containing player digging information
     */
    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            // Packet wrapper
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            DiggingAction actionType = wrapper.getAction();

            // Extract information from wrapper
            Player player = event.getPlayer();

            // Get stages the player is in. If the player is not in any stages, return.
            List<Stage> stages = BlocketAPI.getInstance().getStageManager().getStages(player);
            if (stages == null || stages.isEmpty()) {
                return;
            }

            BlocketPosition position = new BlocketPosition(wrapper.getBlockPosition().getX(), wrapper.getBlockPosition().getY(), wrapper.getBlockPosition().getZ());

            // Find the block in any stage and view using streams
            stages.stream()
                    .filter(stage -> stage.getWorld() == player.getWorld())
                    .flatMap(stage -> stage.getViews().stream())
                    .filter(view -> view.hasBlock(position)).min((view1, view2) -> Integer.compare(view2.getZIndex(), view1.getZIndex()))
                    .ifPresent(view -> {
                        // Get block data from view
                        BlockData blockData = view.getBlock(position);

                        // Call BlocketInteractEvent to handle custom interaction
                        Bukkit.getScheduler().runTaskAsynchronously(BlocketAPI.getInstance().getOwnerPlugin(), () -> new BlocketInteractEvent(player, position, blockData, view, view.getStage()).callEvent());

                        // Check if block is breakable, if not, send block change packet to cancel the break
                        if (!view.isBreakable()) {
                            event.setCancelled(true);
                            return;
                        }

                        // Block break functionality
                        if (actionType == DiggingAction.FINISHED_DIGGING || canInstantBreak(player, blockData)) {
                            BlocketBreakEvent BlocketBreakEvent = new BlocketBreakEvent(player, position, blockData, view, view.getStage());
                            BlocketBreakEvent.callEvent();

                            // Set to air
                            view.setBlock(position, Material.AIR.createBlockData());
                            for (Player audienceMember : view.getStage().getAudience().getOnlinePlayers()) {
                                audienceMember.sendBlockChange(position.toLocation(player.getWorld()), Material.AIR.createBlockData());
                            }

                            // If block is not cancelled, break the block, otherwise, revert the block
                            if (BlocketBreakEvent.isCancelled()) {
                                for (Player audienceMember : view.getStage().getAudience().getOnlinePlayers()) {
                                    audienceMember.sendBlockChange(position.toLocation(player.getWorld()), blockData);
                                }
                                view.setBlock(position, blockData);
                            }
                        }
                    });
        }
    }

    /**
     * Check if player can instantly break block based on tool efficiency, haste effects, and game mode.
     * 
     * <p>Instant break calculation considers:
     * <ul>
     *   <li>Creative mode (always instant)</li>
     *   <li>Tool efficiency and material compatibility</li>
     *   <li>Haste potion effects (20% speed boost per level)</li>
     *   <li>Block hardness vs break speed ratio (30x threshold)</li>
     * </ul>
     * </p>
     *
     * @param player    Player who is digging
     * @param blockData BlockData of the block being broken
     * @return true if the block can be instantly broken, false otherwise
     */
    private boolean canInstantBreak(Player player, BlockData blockData) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        int hasteLevel = player.hasPotionEffect(PotionEffectType.HASTE) ? Objects.requireNonNull(player.getPotionEffect(PotionEffectType.HASTE)).getAmplifier() + 1 : 0;

        // Creative mode always allows instant breaking
        if (player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }

        // Get break speed (already includes efficiency enchantment effects)
        double breakSpeed = blockData.getDestroySpeed(tool, true);

        // Apply haste effect separately
        double hasteMultiplier = 1 + (hasteLevel * 0.2);
        breakSpeed *= hasteMultiplier;

        double hardness = blockData.getMaterial().getHardness();

        // Use a configurable speed multiplier threshold instead of hardcoded magic numbers
        double speedMultiplier = BlocketAPI.getInstance().getConfig().getInstantBreakSpeedMultiplier();
        
        // If break speed is at least (multiplier)x the hardness, it's an instant break
        return breakSpeed >= hardness * speedMultiplier;
    }
}
