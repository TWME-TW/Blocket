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

public class BlockPlaceAdapter extends SimplePacketListenerAbstract {

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
