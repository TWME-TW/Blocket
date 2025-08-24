package dev.twme.blocket.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.twme.blocket.managers.BlockLightingManager;
import dev.twme.blocket.managers.BlockLightingManager.LightingData;
import dev.twme.blocket.types.BlocketPosition;

/**
 * Unit tests for the BlockLightingManager functionality.
 * Tests the core lighting management features including setting,
 * getting, and removing custom lighting data.
 *
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.1.0
 */
class BlockLightingManagerTest {
    
    private BlockLightingManager lightingManager;
    private BlocketPosition testPosition;
    private String testViewName;
    private String testStageName;
    
    @BeforeEach
    void setUp() {
        lightingManager = new BlockLightingManager();
        testPosition = new BlocketPosition(100, 64, 200);
        testViewName = "test-view";
        testStageName = "test-stage";
    }
    
    @Test
    void testSetBlockLight() {
        // Test setting valid block light
        assertTrue(lightingManager.setBlockLight(testViewName, testPosition, 10));
        
        LightingData data = lightingManager.getLighting(testViewName, testPosition);
        assertNotNull(data);
        assertEquals(10, data.getBlockLight());
        assertTrue(data.hasCustomBlockLight());
        assertFalse(data.hasCustomSkyLight());
    }
    
    @Test
    void testSetSkyLight() {
        // Test setting valid sky light
        assertTrue(lightingManager.setSkyLight(testViewName, testPosition, 12));
        
        LightingData data = lightingManager.getLighting(testViewName, testPosition);
        assertNotNull(data);
        assertEquals(12, data.getSkyLight());
        assertTrue(data.hasCustomSkyLight());
        assertFalse(data.hasCustomBlockLight());
    }
    
    @Test
    void testSetBothLights() {
        // Test setting both lights
        assertTrue(lightingManager.setLighting(testViewName, testPosition, 8, 14));
        
        LightingData data = lightingManager.getLighting(testViewName, testPosition);
        assertNotNull(data);
        assertEquals(8, data.getBlockLight());
        assertEquals(14, data.getSkyLight());
        assertTrue(data.hasCustomBlockLight());
        assertTrue(data.hasCustomSkyLight());
    }
    
    @Test
    void testInvalidLightLevels() {
        // Test invalid light levels
        assertFalse(lightingManager.setBlockLight(testViewName, testPosition, -1));
        assertFalse(lightingManager.setBlockLight(testViewName, testPosition, 16));
        assertFalse(lightingManager.setSkyLight(testViewName, testPosition, -1));
        assertFalse(lightingManager.setSkyLight(testViewName, testPosition, 16));
        
        // Verify no data was set
        assertNull(lightingManager.getLighting(testViewName, testPosition));
    }
    
    @Test
    void testRemoveLighting() {
        // Set some lighting first
        lightingManager.setLighting(testViewName, testPosition, 10, 12);
        assertNotNull(lightingManager.getLighting(testViewName, testPosition));
        
        // Remove the lighting
        assertTrue(lightingManager.removeLighting(testViewName, testPosition));
        assertNull(lightingManager.getLighting(testViewName, testPosition));
        
        // Try removing again (should return false)
        assertFalse(lightingManager.removeLighting(testViewName, testPosition));
    }
    
    @Test
    void testRemoveAllViewLighting() {
        // Set lighting for multiple positions
        BlocketPosition pos1 = new BlocketPosition(100, 64, 200);
        BlocketPosition pos2 = new BlocketPosition(101, 64, 201);
        
        lightingManager.setBlockLight(testViewName, pos1, 10);
        lightingManager.setBlockLight(testViewName, pos2, 12);
        
        // Verify both are set
        assertNotNull(lightingManager.getLighting(testViewName, pos1));
        assertNotNull(lightingManager.getLighting(testViewName, pos2));
        
        // Remove all lighting for the view
        assertTrue(lightingManager.removeAllViewLighting(testViewName));
        
        // Verify all are removed
        assertNull(lightingManager.getLighting(testViewName, pos1));
        assertNull(lightingManager.getLighting(testViewName, pos2));
        
        // Try removing again (should return false)
        assertFalse(lightingManager.removeAllViewLighting(testViewName));
    }
    
    @Test
    void testStageLighting() {
        // Test stage-level lighting
        assertTrue(lightingManager.setStageBlockLight(testStageName, testPosition, 15));
        
        LightingData data = lightingManager.getStageLighting(testStageName, testPosition);
        assertNotNull(data);
        assertEquals(15, data.getBlockLight());
        assertTrue(data.hasCustomBlockLight());
    }
    
    @Test
    void testLightingDataMethods() {
        LightingData data = new LightingData(10, 12);
        
        assertEquals(10, data.getBlockLight());
        assertEquals(12, data.getSkyLight());
        assertTrue(data.hasCustomBlockLight());
        assertTrue(data.hasCustomSkyLight());
        
        // Test with updated values
        LightingData updatedBlock = data.withBlockLight(5);
        assertEquals(5, updatedBlock.getBlockLight());
        assertEquals(12, updatedBlock.getSkyLight());
        
        LightingData updatedSky = data.withSkyLight(8);
        assertEquals(10, updatedSky.getBlockLight());
        assertEquals(8, updatedSky.getSkyLight());
    }
    
    @Test
    void testNoCustomLighting() {
        LightingData data = new LightingData(-1, -1);
        
        assertFalse(data.hasCustomBlockLight());
        assertFalse(data.hasCustomSkyLight());
    }
    
    @Test
    void testMultipleViews() {
        String view1 = "view1";
        String view2 = "view2";
        
        // Set different lighting for the same position in different views
        lightingManager.setBlockLight(view1, testPosition, 5);
        lightingManager.setBlockLight(view2, testPosition, 10);
        
        // Verify they are independent
        assertEquals(5, lightingManager.getLighting(view1, testPosition).getBlockLight());
        assertEquals(10, lightingManager.getLighting(view2, testPosition).getBlockLight());
        
        // Remove lighting from one view
        lightingManager.removeLighting(view1, testPosition);
        
        // Verify only that view's lighting was removed
        assertNull(lightingManager.getLighting(view1, testPosition));
        assertNotNull(lightingManager.getLighting(view2, testPosition));
    }
    
    @Test
    void testBoundaryLightLevels() {
        // Test minimum valid light level (0)
        assertTrue(lightingManager.setBlockLight(testViewName, testPosition, 0));
        assertEquals(0, lightingManager.getLighting(testViewName, testPosition).getBlockLight());
        
        // Test maximum valid light level (15)
        assertTrue(lightingManager.setSkyLight(testViewName, testPosition, 15));
        assertEquals(15, lightingManager.getLighting(testViewName, testPosition).getSkyLight());
    }
}
