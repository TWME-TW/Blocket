package dev.twme.blocket.protocol;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.events.BlocketPlaceEvent;
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.View;
import dev.twme.blocket.types.BlocketPosition;

/**
 * Packet adapter for handling block placement interactions in Blocket.
 * This class intercepts block placement packets and manages virtual block consistency.
 */
public class BlockPlaceAdapter extends SimplePacketListenerAbstract {

    /**
     * Handles incoming player block placement packets.
     * Checks if the placement target is a virtual block and fires appropriate events.
     * Cancels placement if the target position contains a virtual block.
     * 
     * @param event The packet event containing block placement information
     */
    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            // Wrapper for the packet
            WrapperPlayClientPlayerBlockPlacement wrapper = new WrapperPlayClientPlayerBlockPlacement(event);
            Player player = event.getPlayer();

            // Get the stages the player is in. If the player is not in any stages, return.
            List<Stage> stages = BlocketAPI.getInstance().getStageManager().getStages(player);
            if (stages == null || stages.isEmpty()) {
                return;
            }

            BlocketPosition position = new BlocketPosition(wrapper.getBlockPosition().getX(), wrapper.getBlockPosition().getY(), wrapper.getBlockPosition().getZ());

            // Check if the block is in any of the views in the stages
            for (Stage stage : stages) {
                if (!stage.getWorld().equals(player.getWorld())) continue;
                for (View view : stage.getViews()) {
                    if (view.hasBlock(position)) {
                        // Call the event and cancel the placement
                        Bukkit.getScheduler().runTask(BlocketAPI.getInstance().getOwnerPlugin(), () -> new BlocketPlaceEvent(player, position, view, stage).callEvent());
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Handles outgoing server block change packets.
     * Prevents the server from sending block change packets that would conflict
     * with virtual blocks, maintaining the consistency of the virtual block display.
     * 
     * @param event The packet event containing block change information
     */
    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);
            Player player = event.getPlayer();

            // Get the stages the player is in. If the player is not in any stages, return.
            List<Stage> stages = BlocketAPI.getInstance().getStageManager().getStages(player);
            if (stages == null || stages.isEmpty()) {
                return;
            }
            
            BlocketPosition position = new BlocketPosition(wrapper.getBlockPosition().getX(), wrapper.getBlockPosition().getY(), wrapper.getBlockPosition().getZ());
            for (Stage stage : stages) {
                if (!stage.getWorld().equals(player.getWorld())) continue;
                for (View view : stage.getViews()) {
                    if (view.hasBlock(position)) {
                        if (wrapper.getBlockState().getType().getName().equalsIgnoreCase(view.getBlock(position).getMaterial().name())) continue;
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

}
