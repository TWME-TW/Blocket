package dev.twme.blocket.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.twme.blocket.constants.ChunkConstants;
import dev.twme.blocket.events.BlockLightingChangeEvent;
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.View;
import dev.twme.blocket.types.BlocketPosition;
import lombok.Getter;
import lombok.NonNull;

/**
 * Manager for handling custom block lighting in Blocket.
 * This class allows setting custom light levels for specific blocks,
 * overriding their natural lighting properties.
 *
 * <p>The lighting system supports both block light (emitted light) and sky light,
 * with values ranging from 0 (no light) to 15 (maximum brightness).
 * Custom lighting settings are applied during chunk processing and
 * affect how blocks appear to players.</p>
 *
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.1.0
 */
@Getter
public class BlockLightingManager {
    private static final Logger LOGGER = Logger.getLogger(BlockLightingManager.class.getName());
    
    /**
     * Maps view name to position-based lighting data
     * Structure: ViewName -> Position -> LightingData
     */
    private final Map<String, Map<BlocketPosition, LightingData>> viewLightingMap;
    
    /**
     * Maps stage name to position-based lighting data
     * Structure: StageName -> Position -> LightingData
     */
    private final Map<String, Map<BlocketPosition, LightingData>> stageLightingMap;
    
    public BlockLightingManager() {
        this.viewLightingMap = new ConcurrentHashMap<>();
        this.stageLightingMap = new ConcurrentHashMap<>();
    }
    
    /**
     * Sets custom block light level for a specific position in a view.
     * Block light represents light emitted by the block itself (like torches, glowstone).
     *
     * @param view The view to set lighting for
     * @param position The position of the block
     * @param blockLight The block light level (0-15)
     * @return true if the lighting was successfully set, false otherwise
     */
    public boolean setBlockLight(@NonNull View view, @NonNull BlocketPosition position, int blockLight) {
        return setBlockLight(view.getName(), position, blockLight);
    }
    
    /**
     * Sets custom block light level for a specific position in a view by name.
     *
     * @param viewName The name of the view
     * @param position The position of the block
     * @param blockLight The block light level (0-15)
     * @return true if the lighting was successfully set, false otherwise
     */
    public boolean setBlockLight(@NonNull String viewName, @NonNull BlocketPosition position, int blockLight) {
        if (!isValidLightLevel(blockLight)) {
            LOGGER.warning(String.format("Invalid block light level %d for position %s in view %s", 
                blockLight, position, viewName));
            return false;
        }
        
        try {
            Map<BlocketPosition, LightingData> viewMap = viewLightingMap.computeIfAbsent(viewName, 
                k -> new ConcurrentHashMap<>());
            
            LightingData existingData = viewMap.get(position);
            LightingData newData = existingData != null 
                ? existingData.withBlockLight(blockLight)
                : new LightingData(blockLight, -1); // -1 means no custom sky light
                
            viewMap.put(position, newData);
            
            // Fire lighting change event
            fireBlockLightingChangeEvent(viewName, position, newData, LightType.BLOCK_LIGHT);
            
            LOGGER.fine(String.format("Set block light level %d for position %s in view %s", 
                blockLight, position, viewName));
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error setting block light for position %s in view %s", 
                position, viewName), e);
            return false;
        }
    }
    
    /**
     * Sets custom sky light level for a specific position in a view.
     * Sky light represents natural sunlight filtering through the world.
     *
     * @param view The view to set lighting for
     * @param position The position of the block
     * @param skyLight The sky light level (0-15)
     * @return true if the lighting was successfully set, false otherwise
     */
    public boolean setSkyLight(@NonNull View view, @NonNull BlocketPosition position, int skyLight) {
        return setSkyLight(view.getName(), position, skyLight);
    }
    
    /**
     * Sets custom sky light level for a specific position in a view by name.
     *
     * @param viewName The name of the view
     * @param position The position of the block
     * @param skyLight The sky light level (0-15)
     * @return true if the lighting was successfully set, false otherwise
     */
    public boolean setSkyLight(@NonNull String viewName, @NonNull BlocketPosition position, int skyLight) {
        if (!isValidLightLevel(skyLight)) {
            LOGGER.warning(String.format("Invalid sky light level %d for position %s in view %s", 
                skyLight, position, viewName));
            return false;
        }
        
        try {
            Map<BlocketPosition, LightingData> viewMap = viewLightingMap.computeIfAbsent(viewName, 
                k -> new ConcurrentHashMap<>());
            
            LightingData existingData = viewMap.get(position);
            LightingData newData = existingData != null 
                ? existingData.withSkyLight(skyLight)
                : new LightingData(-1, skyLight); // -1 means no custom block light
                
            viewMap.put(position, newData);
            
            // Fire lighting change event
            fireBlockLightingChangeEvent(viewName, position, newData, LightType.SKY_LIGHT);
            
            LOGGER.fine(String.format("Set sky light level %d for position %s in view %s", 
                skyLight, position, viewName));
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error setting sky light for position %s in view %s", 
                position, viewName), e);
            return false;
        }
    }
    
    /**
     * Sets both block and sky light levels for a specific position in a view.
     *
     * @param view The view to set lighting for
     * @param position The position of the block
     * @param blockLight The block light level (0-15)
     * @param skyLight The sky light level (0-15)
     * @return true if both lighting values were successfully set, false otherwise
     */
    public boolean setLighting(@NonNull View view, @NonNull BlocketPosition position, 
                              int blockLight, int skyLight) {
        return setLighting(view.getName(), position, blockLight, skyLight);
    }
    
    /**
     * Sets both block and sky light levels for a specific position in a view by name.
     *
     * @param viewName The name of the view
     * @param position The position of the block
     * @param blockLight The block light level (0-15)
     * @param skyLight The sky light level (0-15)
     * @return true if both lighting values were successfully set, false otherwise
     */
    public boolean setLighting(@NonNull String viewName, @NonNull BlocketPosition position, 
                              int blockLight, int skyLight) {
        if (!isValidLightLevel(blockLight) || !isValidLightLevel(skyLight)) {
            LOGGER.warning(String.format("Invalid light levels (block: %d, sky: %d) for position %s in view %s", 
                blockLight, skyLight, position, viewName));
            return false;
        }
        
        try {
            Map<BlocketPosition, LightingData> viewMap = viewLightingMap.computeIfAbsent(viewName, 
                k -> new ConcurrentHashMap<>());
            
            LightingData newData = new LightingData(blockLight, skyLight);
            viewMap.put(position, newData);
            
            // Fire lighting change event
            fireBlockLightingChangeEvent(viewName, position, newData, LightType.BOTH);
            
            LOGGER.fine(String.format("Set lighting (block: %d, sky: %d) for position %s in view %s", 
                blockLight, skyLight, position, viewName));
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error setting lighting for position %s in view %s", 
                position, viewName), e);
            return false;
        }
    }
    
    /**
     * Sets custom block light level for a specific position in a stage.
     *
     * @param stage The stage to set lighting for
     * @param position The position of the block
     * @param blockLight The block light level (0-15)
     * @return true if the lighting was successfully set, false otherwise
     */
    public boolean setStageBlockLight(@NonNull Stage stage, @NonNull BlocketPosition position, int blockLight) {
        return setStageBlockLight(stage.getName(), position, blockLight);
    }
    
    /**
     * Sets custom block light level for a specific position in a stage by name.
     *
     * @param stageName The name of the stage
     * @param position The position of the block
     * @param blockLight The block light level (0-15)
     * @return true if the lighting was successfully set, false otherwise
     */
    public boolean setStageBlockLight(@NonNull String stageName, @NonNull BlocketPosition position, int blockLight) {
        if (!isValidLightLevel(blockLight)) {
            LOGGER.warning(String.format("Invalid block light level %d for position %s in stage %s", 
                blockLight, position, stageName));
            return false;
        }
        
        try {
            Map<BlocketPosition, LightingData> stageMap = stageLightingMap.computeIfAbsent(stageName, 
                k -> new ConcurrentHashMap<>());
            
            LightingData existingData = stageMap.get(position);
            LightingData newData = existingData != null 
                ? existingData.withBlockLight(blockLight)
                : new LightingData(blockLight, -1);
                
            stageMap.put(position, newData);
            
            LOGGER.fine(String.format("Set stage block light level %d for position %s in stage %s", 
                blockLight, position, stageName));
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error setting stage block light for position %s in stage %s", 
                position, stageName), e);
            return false;
        }
    }
    
    /**
     * Gets the custom lighting data for a specific position in a view.
     *
     * @param viewName The name of the view
     * @param position The position to check
     * @return The lighting data, or null if no custom lighting is set
     */
    public LightingData getLighting(@NonNull String viewName, @NonNull BlocketPosition position) {
        Map<BlocketPosition, LightingData> viewMap = viewLightingMap.get(viewName);
        return viewMap != null ? viewMap.get(position) : null;
    }
    
    /**
     * Gets the custom lighting data for a specific position in a stage.
     *
     * @param stageName The name of the stage
     * @param position The position to check
     * @return The lighting data, or null if no custom lighting is set
     */
    public LightingData getStageLighting(@NonNull String stageName, @NonNull BlocketPosition position) {
        Map<BlocketPosition, LightingData> stageMap = stageLightingMap.get(stageName);
        return stageMap != null ? stageMap.get(position) : null;
    }
    
    /**
     * Removes custom lighting for a specific position in a view.
     *
     * @param viewName The name of the view
     * @param position The position to remove lighting from
     * @return true if lighting was removed, false if no custom lighting existed
     */
    public boolean removeLighting(@NonNull String viewName, @NonNull BlocketPosition position) {
        Map<BlocketPosition, LightingData> viewMap = viewLightingMap.get(viewName);
        if (viewMap != null) {
            LightingData removed = viewMap.remove(position);
            if (removed != null) {
                // Clean up empty maps
                if (viewMap.isEmpty()) {
                    viewLightingMap.remove(viewName);
                }
                
                LOGGER.fine(String.format("Removed custom lighting for position %s in view %s", 
                    position, viewName));
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes all custom lighting for a specific view.
     *
     * @param viewName The name of the view
     * @return true if lighting was removed, false if no custom lighting existed
     */
    public boolean removeAllViewLighting(@NonNull String viewName) {
        Map<BlocketPosition, LightingData> removed = viewLightingMap.remove(viewName);
        if (removed != null && !removed.isEmpty()) {
            LOGGER.fine(String.format("Removed all custom lighting for view %s (%d positions)", 
                viewName, removed.size()));
            return true;
        }
        return false;
    }
    
    /**
     * Checks if a light level is valid (0-15).
     *
     * @param lightLevel The light level to validate
     * @return true if the light level is valid
     */
    private boolean isValidLightLevel(int lightLevel) {
        return lightLevel >= 0 && lightLevel <= ChunkConstants.MAX_LIGHT_LEVEL;
    }
    
    /**
     * Fires a block lighting change event.
     *
     * @param viewName The view name
     * @param position The position
     * @param lightingData The new lighting data
     * @param lightType The type of light that changed
     */
    private void fireBlockLightingChangeEvent(String viewName, BlocketPosition position, 
                                            LightingData lightingData, LightType lightType) {
        try {
            BlockLightingChangeEvent event = new BlockLightingChangeEvent(viewName, position, 
                lightingData, lightType);
            // Fire event using Bukkit's event system
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error firing block lighting change event", e);
        }
    }
    
    /**
     * Data class for storing lighting information.
     */
    @Getter
    public static class LightingData {
        private final int blockLight;
        private final int skyLight;
        
        public LightingData(int blockLight, int skyLight) {
            this.blockLight = blockLight;
            this.skyLight = skyLight;
        }
        
        /**
         * Returns a new LightingData with updated block light.
         */
        public LightingData withBlockLight(int newBlockLight) {
            return new LightingData(newBlockLight, this.skyLight);
        }
        
        /**
         * Returns a new LightingData with updated sky light.
         */
        public LightingData withSkyLight(int newSkyLight) {
            return new LightingData(this.blockLight, newSkyLight);
        }
        
        /**
         * Checks if block light is set (not -1).
         */
        public boolean hasCustomBlockLight() {
            return blockLight >= 0;
        }
        
        /**
         * Checks if sky light is set (not -1).
         */
        public boolean hasCustomSkyLight() {
            return skyLight >= 0;
        }
        
        @Override
        public String toString() {
            return String.format("LightingData{blockLight=%d, skyLight=%d}", blockLight, skyLight);
        }
    }
    
    /**
     * Enum representing the type of lighting change.
     */
    public enum LightType {
        BLOCK_LIGHT,
        SKY_LIGHT,
        BOTH
    }
}
