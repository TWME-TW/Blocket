# Migration Guide
This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Upgrade between Blocket versions and migrate from other virtual block systems.

## Version Migration

### From Pre-1.0 to 1.0.0

**API Changes**:

The initialization process has been simplified in version 1.0.0:

**Old (Pre-1.0)**:
```java
// Old initialization method
BlocketManager manager = new BlocketManager();
manager.initialize(this);
manager.enableListeners();
```

**New (1.0.0+)**:
```java
// New unified initialization
BlocketConfig config = BlocketConfig.builder()
    .enableBreakListener(true)
    .enableInteractListener(true)
    .build();

BlocketAPI.initialize(config);
BlocketAPI api = BlocketAPI.getInstance();
```

**Configuration Migration**:

**Old Configuration**:
```yaml
# config.yml
blocket:
  enable-break-listener: true
  enable-interact-listener: true
  max-audience-size: 50
```

**New Configuration** (Code-based):
```java
BlocketConfig config = BlocketConfig.builder()
    .enableBreakListener(true)
    .enableInteractListener(true)
    .maxAudienceSize(50)
    .build();
```

**Stage Creation Changes**:

**Old**:
```java
// Old stage creation
BlocketStage stage = manager.createStage("my_stage");
stage.setBounds(world, pos1, pos2);
stage.setAudience(players);
```

**New**:
```java
// New stage creation
Audience audience = Audience.fromPlayers(players);
Stage stage = new Stage("my_stage", world, pos1, pos2, audience);
stageManager.addStage(stage);
```

### Migration Script

Create a migration helper to convert old data:

```java
public class BlocketMigrationHelper {
    private final JavaPlugin plugin;
    private final Logger logger;
    
    public BlocketMigrationHelper(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    public void performMigration() {
        logger.info("Starting Blocket migration...");
        
        // Step 1: Detect old version
        String oldVersion = detectOldVersion();
        if (oldVersion == null) {
            logger.info("No old version detected - fresh installation");
            return;
        }
        
        logger.info("Migrating from version: " + oldVersion);
        
        // Step 2: Backup old data
        backupOldData();
        
        // Step 3: Migrate based on version
        switch (oldVersion) {
            case "0.9.x":
                migrateFrom09x();
                break;
            case "0.8.x":
                migrateFrom08x();
                break;
            default:
                logger.warning("Unknown old version: " + oldVersion);
        }
        
        // Step 4: Initialize new API
        initializeNewAPI();
        
        // Step 5: Verify migration
        verifyMigration();
        
        logger.info("Migration completed successfully!");
    }
    
    private String detectOldVersion() {
        // Check for old config files
        File oldConfig = new File(plugin.getDataFolder(), "old_config.yml");
        if (oldConfig.exists()) {
            // Parse version from old config
            return parseVersionFromOldConfig(oldConfig);
        }
        
        // Check for old data files
        File oldData = new File(plugin.getDataFolder(), "stages.dat");
        if (oldData.exists()) {
            return "0.9.x"; // Assume 0.9.x if stages.dat exists
        }
        
        return null; // No old version found
    }
    
    private void backupOldData() {
        File dataFolder = plugin.getDataFolder();
        File backupFolder = new File(dataFolder, "backup_" + System.currentTimeMillis());
        backupFolder.mkdirs();
        
        try {
            // Backup all old files
            Files.walk(dataFolder.toPath())
                .filter(Files::isRegularFile)
                .forEach(source -> {
                    try {
                        Path target = backupFolder.toPath().resolve(dataFolder.toPath().relativize(source));
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target);
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Failed to backup file: " + source, e);
                    }
                });
                
            logger.info("Backup created at: " + backupFolder.getName());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create backup", e);
        }
    }
    
    private void migrateFrom09x() {
        logger.info("Migrating from 0.9.x...");
        
        // Migrate old stage files
        migrateOldStages();
        
        // Migrate old configuration
        migrateOldConfig();
        
        // Convert old event handlers
        migrateOldEventHandlers();
    }
    
    private void migrateOldStages() {
        File stagesFile = new File(plugin.getDataFolder(), "stages.dat");
        if (!stagesFile.exists()) {
            return;
        }
        
        try {
            // Read old stage data (example format)
            List<OldStageData> oldStages = readOldStageData(stagesFile);
            
            // Convert to new format
            BlocketAPI api = BlocketAPI.getInstance();
            StageManager stageManager = api.getStageManager();
            
            for (OldStageData oldStage : oldStages) {
                // Convert old stage to new stage
                Audience audience = convertOldAudience(oldStage.getPlayers());
                
                Stage newStage = new Stage(
                    oldStage.getId(),
                    oldStage.getWorld(),
                    convertOldPosition(oldStage.getPos1()),
                    convertOldPosition(oldStage.getPos2()),
                    audience
                );
                
                // Migrate views
                for (OldViewData oldView : oldStage.getViews()) {
                    View newView = convertOldView(oldView);
                    newStage.addView(newView);
                }
                
                stageManager.addStage(newStage);
                logger.info("Migrated stage: " + oldStage.getId());
            }
            
            // Archive old file
            Files.move(stagesFile.toPath(), 
                new File(plugin.getDataFolder(), "stages.dat.old").toPath(),
                StandardCopyOption.REPLACE_EXISTING);
                
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to migrate stages", e);
        }
    }
    
    private void migrateOldConfig() {
        File oldConfigFile = new File(plugin.getDataFolder(), "config.yml");
        if (!oldConfigFile.exists()) {
            return;
        }
        
        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile);
        
        // Create new configuration
        BlocketConfig.Builder configBuilder = BlocketConfig.builder();
        
        // Map old settings to new ones
        if (oldConfig.contains("enable-break-listener")) {
            configBuilder.enableBreakListener(oldConfig.getBoolean("enable-break-listener"));
        }
        
        if (oldConfig.contains("enable-interact-listener")) {
            configBuilder.enableInteractListener(oldConfig.getBoolean("enable-interact-listener"));
        }
        
        if (oldConfig.contains("max-audience-size")) {
            configBuilder.maxAudienceSize(oldConfig.getInt("max-audience-size"));
        }
        
        // Save migrated configuration
        BlocketConfig newConfig = configBuilder.build();
        saveNewConfiguration(newConfig);
        
        logger.info("Configuration migrated successfully");
    }
    
    private void migrateOldEventHandlers() {
        // This is more complex - may require plugin developer intervention
        logger.info("=== Event Handler Migration Required ===");
        logger.info("Old event handlers need manual migration:");
        logger.info("1. Replace 'BlocketBlockBreakEvent' with 'BlocketBreakEvent'");
        logger.info("2. Replace 'BlocketBlockInteractEvent' with 'BlocketInteractEvent'");
        logger.info("3. Update event handler method signatures");
        logger.info("4. See migration documentation for details");
        logger.info("=====================================");
    }
    
    // Helper classes for old data structures
    private static class OldStageData {
        private String id;
        private World world;
        private OldPosition pos1, pos2;
        private List<UUID> players;
        private List<OldViewData> views;
        
        // Getters and setters
        public String getId() { return id; }
        public World getWorld() { return world; }
        public OldPosition getPos1() { return pos1; }
        public OldPosition getPos2() { return pos2; }
        public List<UUID> getPlayers() { return players; }
        public List<OldViewData> getViews() { return views; }
    }
    
    private static class OldViewData {
        private String id;
        private Map<OldPosition, Material> blocks;
        
        public String getId() { return id; }
        public Map<OldPosition, Material> getBlocks() { return blocks; }
    }
    
    private static class OldPosition {
        private int x, y, z;
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
    }
    
    // Conversion helper methods
    private Audience convertOldAudience(List<UUID> playerUUIDs) {
        Set<UUID> uuidSet = new HashSet<>(playerUUIDs);
        return Audience.fromUUIDs(uuidSet);
    }
    
    private BlocketPosition convertOldPosition(OldPosition oldPos) {
        return new BlocketPosition(oldPos.getX(), oldPos.getY(), oldPos.getZ());
    }
    
    private View convertOldView(OldViewData oldView) {
        // Implementation depends on View constructor
        View newView = new View(oldView.getId());
        
        // Convert blocks
        for (Map.Entry<OldPosition, Material> entry : oldView.getBlocks().entrySet()) {
            BlocketPosition newPos = convertOldPosition(entry.getKey());
            newView.setBlock(newPos, entry.getValue());
        }
        
        return newView;
    }
}
```

## Migrating from Other Systems

### From FastAsyncWorldEdit (FAWE)

If you're migrating from FAWE virtual blocks:

```java
public class FAWEMigrationHelper {
    
    public void migrateFAWEStructure(Player player, Region region) {
        // Get FAWE virtual blocks in region
        World world = player.getWorld();
        
        // Create Blocket stage for the same area
        BlocketPosition pos1 = new BlocketPosition(
            region.getMinimumPoint().getBlockX(),
            region.getMinimumPoint().getBlockY(),
            region.getMinimumPoint().getBlockZ()
        );
        
        BlocketPosition pos2 = new BlocketPosition(
            region.getMaximumPoint().getBlockX(),
            region.getMaximumPoint().getBlockY(),
            region.getMaximumPoint().getBlockZ()
        );
        
        Audience audience = Audience.fromPlayers(Set.of(player));
        Stage stage = new Stage("migrated_fawe", world, pos1, pos2, audience);
        
        // Create view and copy blocks
        View view = new View("fawe_blocks");
        
        // Iterate through region and copy blocks
        for (int x = pos1.getX(); x <= pos2.getX(); x++) {
            for (int y = pos1.getY(); y <= pos2.getY(); y++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    BlocketPosition blockPos = new BlocketPosition(x, y, z);
                    Location location = new Location(world, x, y, z);
                    
                    // Get the actual block material (may be virtual in FAWE)
                    Material material = location.getBlock().getType();
                    
                    if (material != Material.AIR) {
                        view.setBlock(blockPos, material);
                    }
                }
            }
        }
        
        stage.addView(view);
        BlocketAPI.getInstance().getStageManager().addStage(stage);
        
        player.sendMessage("§aMigrated FAWE structure to Blocket!");
    }
}
```

### From ProtocolLib Virtual Blocks

Migrating from manual ProtocolLib packet handling:

```java
public class ProtocolLibMigration {
    
    // Old ProtocolLib method
    public void sendVirtualBlockOld(Player player, Location location, Material material) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        packet.getBlockPositionModifier().write(0, new BlockPosition(location.toVector()));
        packet.getBlockData().write(0, WrappedBlockData.createData(material));
        
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    // New Blocket method
    public void sendVirtualBlockNew(Player player, Location location, Material material) {
        // Create a temporary stage for this block
        BlocketPosition pos = new BlocketPosition(
            location.getBlockX(),
            location.getBlockY(), 
            location.getBlockZ()
        );
        
        Audience audience = Audience.fromPlayers(Set.of(player));
        Stage stage = new Stage("temp_block", location.getWorld(), pos, pos, audience);
        
        View view = new View("single_block");
        view.setBlock(pos, material);
        stage.addView(view);
        
        BlocketAPI.getInstance().getStageManager().addStage(stage);
        
        // Send blocks
        view.sendBlocksToAudience();
    }
    
    public void migrateProtocolLibHandler(ProtocolManager protocolManager) {
        // Remove old ProtocolLib listeners
        protocolManager.removePacketListeners(yourPlugin);
        
        // Replace with Blocket event handlers
        Bukkit.getPluginManager().registerEvents(new BlocketEventHandler(), yourPlugin);
        
        logger.info("Migrated from ProtocolLib to Blocket event system");
    }
}
```

### From Citizens NPC Holograms

Converting NPC hologram systems to Blocket:

```java
public class CitizensMigration {
    
    public void migrateNPCHologram(NPC npc) {
        Location npcLocation = npc.getEntity().getLocation();
        
        // Create hologram stage above NPC
        BlocketPosition pos1 = new BlocketPosition(
            npcLocation.getBlockX() - 1,
            npcLocation.getBlockY() + 3,
            npcLocation.getBlockZ() - 1
        );
        
        BlocketPosition pos2 = new BlocketPosition(
            npcLocation.getBlockX() + 1,
            npcLocation.getBlockY() + 5,
            npcLocation.getBlockZ() + 1
        );
        
        // All players can see this hologram
        Set<Player> allPlayers = new HashSet<>(Bukkit.getOnlinePlayers());
        Audience audience = Audience.fromPlayers(allPlayers);
        
        Stage hologramStage = new Stage(
            "npc_hologram_" + npc.getId(),
            npcLocation.getWorld(),
            pos1, pos2,
            audience
        );
        
        // Create hologram blocks (floating glowstone, etc.)
        View hologramView = new View("hologram_blocks");
        
        // Create a simple "!" icon above NPC
        BlocketPosition centerPos = new BlocketPosition(
            npcLocation.getBlockX(),
            npcLocation.getBlockY() + 4,
            npcLocation.getBlockZ()
        );
        
        hologramView.setBlock(centerPos, Material.GLOWSTONE);
        
        hologramStage.addView(hologramView);
        BlocketAPI.getInstance().getStageManager().addStage(hologramStage);
        
        // Update hologram when players approach
        scheduleHologramUpdates(hologramStage, npcLocation);
    }
    
    private void scheduleHologramUpdates(Stage hologramStage, Location npcLocation) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Update audience based on nearby players
            Set<Player> nearbyPlayers = npcLocation.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distance(npcLocation) <= 20)
                .collect(Collectors.toSet());
            
            // Update audience (if API supports this)
            Audience newAudience = Audience.fromPlayers(nearbyPlayers);
            // hologramStage.updateAudience(newAudience);
            
        }, 0L, 40L); // Update every 2 seconds
    }
}
```

## Database Migration

### Schema Migration

If you're changing database schemas between versions:

```java
public class BlocketDatabaseMigrator {
    private final Connection connection;
    private int currentVersion;
    
    public void performDatabaseMigration() throws SQLException {
        currentVersion = getCurrentSchemaVersion();
        
        if (currentVersion < 1) {
            migrateToVersion1();
        }
        if (currentVersion < 2) {
            migrateToVersion2();
        }
        if (currentVersion < 3) {
            migrateToVersion3();
        }
        
        updateSchemaVersion();
    }
    
    private void migrateToVersion1() throws SQLException {
        logger.info("Migrating database to version 1...");
        
        // Create new tables
        String createStagesTable = """
            CREATE TABLE IF NOT EXISTS blocket_stages (
                id VARCHAR(255) PRIMARY KEY,
                world_name VARCHAR(255) NOT NULL,
                pos1_x INT NOT NULL,
                pos1_y INT NOT NULL,
                pos1_z INT NOT NULL,
                pos2_x INT NOT NULL,
                pos2_y INT NOT NULL,
                pos2_z INT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        String createViewsTable = """
            CREATE TABLE IF NOT EXISTS blocket_views (
                id VARCHAR(255) PRIMARY KEY,
                stage_id VARCHAR(255) NOT NULL,
                blocks_data TEXT,
                FOREIGN KEY (stage_id) REFERENCES blocket_stages(id)
            )
        """;
        
        String createAudiencesTable = """
            CREATE TABLE IF NOT EXISTS blocket_audiences (
                stage_id VARCHAR(255) NOT NULL,
                player_uuid VARCHAR(36) NOT NULL,
                added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (stage_id, player_uuid),
                FOREIGN KEY (stage_id) REFERENCES blocket_stages(id)
            )
        """;
        
        connection.createStatement().execute(createStagesTable);
        connection.createStatement().execute(createViewsTable);
        connection.createStatement().execute(createAudiencesTable);
        
        currentVersion = 1;
        logger.info("Database migrated to version 1");
    }
    
    private void migrateToVersion2() throws SQLException {
        logger.info("Migrating database to version 2...");
        
        // Add new columns
        String addPatternColumn = "ALTER TABLE blocket_views ADD COLUMN pattern_data TEXT";
        String addMetadataColumn = "ALTER TABLE blocket_stages ADD COLUMN metadata TEXT";
        
        try {
            connection.createStatement().execute(addPatternColumn);
            connection.createStatement().execute(addMetadataColumn);
        } catch (SQLException e) {
            // Column might already exist - check error type
            if (!e.getMessage().contains("duplicate column")) {
                throw e;
            }
        }
        
        currentVersion = 2;
        logger.info("Database migrated to version 2");
    }
    
    private void migrateToVersion3() throws SQLException {
        logger.info("Migrating database to version 3...");
        
        // Data migration - convert old format to new format
        String selectOldData = "SELECT id, blocks_data FROM blocket_views WHERE pattern_data IS NULL";
        String updateNewData = "UPDATE blocket_views SET pattern_data = ? WHERE id = ?";
        
        try (PreparedStatement select = connection.prepareStatement(selectOldData);
             PreparedStatement update = connection.prepareStatement(updateNewData)) {
            
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                String viewId = rs.getString("id");
                String oldBlocksData = rs.getString("blocks_data");
                
                // Convert old blocks format to new pattern format
                String newPatternData = convertBlocksToPattern(oldBlocksData);
                
                update.setString(1, newPatternData);
                update.setString(2, viewId);
                update.addBatch();
            }
            
            update.executeBatch();
        }
        
        currentVersion = 3;
        logger.info("Database migrated to version 3");
    }
    
    private String convertBlocksToPattern(String oldBlocksData) {
        // Implementation depends on your old data format
        // This is just an example
        return oldBlocksData.replace("old_format", "new_pattern_format");
    }
    
    private int getCurrentSchemaVersion() throws SQLException {
        String query = """
            SELECT version FROM blocket_schema_version 
            ORDER BY applied_at DESC LIMIT 1
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("version");
            }
        } catch (SQLException e) {
            // Table doesn't exist - version 0
            createSchemaVersionTable();
        }
        
        return 0;
    }
    
    private void createSchemaVersionTable() throws SQLException {
        String createTable = """
            CREATE TABLE blocket_schema_version (
                version INT PRIMARY KEY,
                applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        connection.createStatement().execute(createTable);
    }
    
    private void updateSchemaVersion() throws SQLException {
        String insert = "INSERT INTO blocket_schema_version (version) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            stmt.setInt(1, currentVersion);
            stmt.executeUpdate();
        }
    }
}
```

## Configuration Migration

### YAML to Code-based Config

Migrate from file-based to code-based configuration:

```java
public class ConfigMigrationHelper {
    
    public BlocketConfig migrateFromYaml(File yamlFile) {
        if (!yamlFile.exists()) {
            return BlocketConfig.builder().build(); // Default config
        }
        
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(yamlFile);
        BlocketConfig.Builder builder = BlocketConfig.builder();
        
        // Migrate settings
        migrateListenerSettings(yaml, builder);
        migratePerformanceSettings(yaml, builder);
        migrateFeatureSettings(yaml, builder);
        
        return builder.build();
    }
    
    private void migrateListenerSettings(YamlConfiguration yaml, BlocketConfig.Builder builder) {
        ConfigurationSection listeners = yaml.getConfigurationSection("listeners");
        if (listeners != null) {
            builder.enableBreakListener(listeners.getBoolean("break", true));
            builder.enableInteractListener(listeners.getBoolean("interact", true));
            builder.enablePlaceListener(listeners.getBoolean("place", false));
        }
    }
    
    private void migratePerformanceSettings(YamlConfiguration yaml, BlocketConfig.Builder builder) {
        ConfigurationSection performance = yaml.getConfigurationSection("performance");
        if (performance != null) {
            builder.maxAudienceSize(performance.getInt("max-audience-size", 100));
            builder.chunkLoadRadius(performance.getInt("chunk-load-radius", 5));
            builder.updateInterval(performance.getInt("update-interval", 50));
        }
    }
    
    private void migrateFeatureSettings(YamlConfiguration yaml, BlocketConfig.Builder builder) {
        ConfigurationSection features = yaml.getConfigurationSection("features");
        if (features != null) {
            builder.enableAsyncProcessing(features.getBoolean("async-processing", true));
            builder.enableCaching(features.getBoolean("caching", true));
            builder.enableDebugMode(features.getBoolean("debug", false));
        }
    }
    
    public void saveConfigTemplate(BlocketConfig config, File outputFile) throws IOException {
        // Create a YAML template showing the equivalent settings
        StringBuilder template = new StringBuilder();
        template.append("# Blocket Configuration Template\n");
        template.append("# This shows the equivalent YAML for your current code-based config\n\n");
        
        template.append("listeners:\n");
        template.append("  break: ").append(config.isBreakListenerEnabled()).append("\n");
        template.append("  interact: ").append(config.isInteractListenerEnabled()).append("\n");
        template.append("  place: ").append(config.isPlaceListenerEnabled()).append("\n\n");
        
        template.append("performance:\n");
        template.append("  max-audience-size: ").append(config.getMaxAudienceSize()).append("\n");
        template.append("  chunk-load-radius: ").append(config.getChunkLoadRadius()).append("\n");
        template.append("  update-interval: ").append(config.getUpdateInterval()).append("\n\n");
        
        Files.write(outputFile.toPath(), template.toString().getBytes());
        logger.info("Configuration template saved to: " + outputFile.getName());
    }
}
```

## Testing Migration

### Migration Test Suite

Always test your migration thoroughly:

```java
public class MigrationTestSuite {
    
    @Test
    public void testDataIntegrity() {
        // Test that all data is preserved during migration
        List<StageData> originalData = captureStageData();
        
        // Perform migration
        performMigration();
        
        // Verify data integrity
        List<StageData> migratedData = captureStageData();
        assertEquals(originalData.size(), migratedData.size());
        
        for (int i = 0; i < originalData.size(); i++) {
            StageData original = originalData.get(i);
            StageData migrated = migratedData.get(i);
            
            assertEquals(original.getId(), migrated.getId());
            assertEquals(original.getWorldName(), migrated.getWorldName());
            assertEquals(original.getBounds(), migrated.getBounds());
        }
    }
    
    @Test
    public void testPerformanceMigration() {
        // Test that migration doesn't cause performance regression
        long startTime = System.currentTimeMillis();
        
        performMigration();
        
        long migrationTime = System.currentTimeMillis() - startTime;
        assertTrue("Migration took too long: " + migrationTime + "ms", 
            migrationTime < 30000); // Should complete within 30 seconds
    }
    
    @Test
    public void testBackwardCompatibility() {
        // Test that migrated data works with new API
        performMigration();
        
        BlocketAPI api = BlocketAPI.getInstance();
        assertNotNull("API not initialized after migration", api);
        
        StageManager stageManager = api.getStageManager();
        assertNotNull("StageManager not available after migration", stageManager);
        
        // Test basic operations
        Collection<Stage> stages = stageManager.getStages();
        assertFalse("No stages found after migration", stages.isEmpty());
        
        for (Stage stage : stages) {
            assertNotNull("Stage has null ID", stage.getId());
            assertNotNull("Stage has null world", stage.getWorld());
            assertNotNull("Stage has null audience", stage.getAudience());
        }
    }
}
```

## Migration Checklist

### Pre-Migration

- [ ] **Backup all data** (files, database, configurations)
- [ ] **Test migration on development server first**
- [ ] **Document current plugin versions and configurations**
- [ ] **Verify server has enough disk space for migration**
- [ ] **Schedule maintenance window for production migration**

### During Migration

- [ ] **Stop the server gracefully**
- [ ] **Run migration scripts**
- [ ] **Verify logs for errors**
- [ ] **Test basic functionality before going live**
- [ ] **Monitor performance after restart**

### Post-Migration

- [ ] **Verify all stages are working correctly**
- [ ] **Check event handlers are functioning**
- [ ] **Monitor server performance for first hour**
- [ ] **Test with a few trusted players first**
- [ ] **Archive old data files safely**

### Rollback Plan

Always have a rollback plan ready:

```java
public class RollbackHelper {
    
    public void createRollbackScript() {
        StringBuilder rollback = new StringBuilder();
        rollback.append("#!/bin/bash\n");
        rollback.append("# Blocket Migration Rollback Script\n\n");
        rollback.append("echo \"Starting Blocket rollback...\"\n\n");
        rollback.append("# Stop server\n");
        rollback.append("screen -S minecraft -X stuff 'stop^M'\n");
        rollback.append("sleep 10\n\n");
        rollback.append("# Restore backup\n");
        rollback.append("cp -r backup_* plugins/YourPlugin/\n\n");
        rollback.append("# Restart server\n");
        rollback.append("screen -dmS minecraft java -jar server.jar\n\n");
        rollback.append("echo \"Rollback completed!\"\n");
        
        try {
            Files.write(Paths.get("rollback.sh"), rollback.toString().getBytes());
            // Make executable
            Runtime.getRuntime().exec("chmod +x rollback.sh");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create rollback script", e);
        }
    }
}
```

---

**Always test migrations thoroughly on a development server before applying to production!**
