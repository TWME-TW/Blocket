package dev.twme.blocket.protocol;

import java.util.List;

import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.types.BlocketChunk;

public class ChunkLoadAdapter extends SimplePacketListenerAbstract {

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.CHUNK_DATA) {
            Player player = event.getPlayer();

            // Wrapper for the chunk data packet
            WrapperPlayServerChunkData chunkData = new WrapperPlayServerChunkData(event);
            int chunkX = chunkData.getColumn().getX();
            int chunkZ = chunkData.getColumn().getZ();

            // Get the stages the player is in. If the player is not in any stages, return.
            List<Stage> stages = BlocketAPI.getInstance().getStageManager().getStages(player);
            if (stages == null || stages.isEmpty()) {
                return;
            }

            // Loop through the stages and views to check if the chunk is in the view.
            for (Stage stage : stages) {

                // If the chunk is not in the world, return.
                if (!stage.getWorld().equals(player.getWorld())) return;

                if (stage.getChunks().contains(new BlocketChunk(chunkX, chunkZ))) {
                    BlocketChunk BlocketChunk = new BlocketChunk(chunkX, chunkZ);

                    // Cancel the packet to prevent the player from seeing the chunk
                    event.setCancelled(true);

                    // Send the chunk packet to the player
                    BlocketAPI.getInstance().getBlockChangeManager().getExecutorService().submit(() -> BlocketAPI.getInstance().getBlockChangeManager().sendChunkPacket(player, BlocketChunk, false));
                }
            }
        }
    }

}
