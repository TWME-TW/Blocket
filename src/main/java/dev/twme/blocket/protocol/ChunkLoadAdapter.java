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

/**
 * Packet adapter that handles chunk loading for virtual blocks.
 * This adapter intercepts chunk data packets being sent to clients
 * and replaces them with custom chunk data that includes virtual blocks.
 * 
 * <p>The adapter handles:
 * <ul>
 *   <li>Detection of chunks that contain virtual blocks</li>
 *   <li>Cancellation of original chunk packets</li>
 *   <li>Generation of custom chunk packets with virtual block data</li>
 *   <li>Asynchronous chunk processing for performance</li>
 * </ul>
 * </p>
 * 
 * <p>This adapter is crucial for ensuring that players see virtual blocks
 * when chunks are loaded or reloaded, maintaining consistency between
 * server state and client display.</p>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChunkLoadAdapter extends SimplePacketListenerAbstract {

    /**
     * Handles outgoing chunk data packets to replace them with virtual block data.
     * When a chunk contains virtual blocks for the receiving player, the original
     * chunk packet is cancelled and a custom chunk packet is sent instead.
     * 
     * <p>This method:
     * <ul>
     *   <li>Intercepts CHUNK_DATA packets</li>
     *   <li>Checks if the chunk contains virtual blocks for the player</li>
     *   <li>Cancels the original packet if virtual blocks are present</li>
     *   <li>Submits custom chunk generation to the executor service</li>
     * </ul>
     * </p>
     * 
     * @param event The packet event containing chunk data information
     */
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
