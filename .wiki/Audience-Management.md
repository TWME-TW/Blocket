# Audience Management
This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Master the audience system to control who can see and interact with your virtual blocks.

## Understanding Audiences

An **Audience** in Blocket represents a group of players who can see and interact with virtual blocks within a stage. Think of it as a "visibility group" that determines which players receive block updates and can interact with virtual content.

## Core Concepts

### Player Visibility

```java
// Only these players can see the virtual blocks
Set<Player> allowedPlayers = Set.of(player1, player2, player3);
Audience audience = Audience.fromPlayers(allowedPlayers);

// Virtual blocks are invisible to all other players
```

### Online vs Offline Players

Audiences can contain both online and offline players:

```java
// For online players only
Set<Player> onlinePlayers = Set.of(player1, player2);
Audience onlineAudience = Audience.fromPlayers(onlinePlayers);

// For offline players (using UUIDs)
Set<UUID> allMembers = Set.of(uuid1, uuid2, uuid3); // Mix of online/offline
Audience persistentAudience = Audience.fromUUIDs(allMembers);
```

## Creating Audiences

### From Online Players

```java
// Single player audience
Player player = event.getPlayer();
Audience soloAudience = Audience.fromPlayers(Set.of(player));

// Multiple players
Set<Player> teamPlayers = new HashSet<>();
teamPlayers.add(player1);
teamPlayers.add(player2);
teamPlayers.add(player3);
Audience teamAudience = Audience.fromPlayers(teamPlayers);

// From collection
List<Player> partyMembers = getPartyMembers(partyLeader);
Audience partyAudience = Audience.fromPlayers(new HashSet<>(partyMembers));
```

### From Player UUIDs

```java
// Persistent audience (works with offline players)
Set<UUID> guildMembers = guild.getMemberUUIDs();
Audience guildAudience = Audience.fromUUIDs(guildMembers);

// Mixed online and offline
Set<UUID> friendsList = player.getFriends(); // May include offline players
Audience friendsAudience = Audience.fromUUIDs(friendsList);
```

### Dynamic Audience Creation

```java
public class DynamicAudienceFactory {
    
    public Audience createLocationBasedAudience(Location center, double radius) {
        return center.getWorld().getPlayers().stream()
            .filter(player -> player.getLocation().distance(center) <= radius)
            .collect(Collectors.collectingAndThen(
                Collectors.toSet(),
                Audience::fromPlayers
            ));
    }
    
    public Audience createPermissionBasedAudience(String permission) {
        return Bukkit.getOnlinePlayers().stream()
            .filter(player -> player.hasPermission(permission))
            .collect(Collectors.collectingAndThen(
                Collectors.toSet(),
                Audience::fromPlayers
            ));
    }
    
    public Audience createLevelBasedAudience(int minLevel) {
        return Bukkit.getOnlinePlayers().stream()
            .filter(player -> player.getLevel() >= minLevel)
            .collect(Collectors.collectingAndThen(
                Collectors.toSet(),
                Audience::fromPlayers
            ));
    }
}
```

## Modifying Audiences

### Adding Players

```java
Audience audience = Audience.fromPlayers(Set.of(player1));

// Add new players dynamically
audience.addPlayer(player2);
audience.addPlayer(player3);

// Add from UUID (supports offline players)
audience.addPlayer(offlinePlayerUUID);
```

### Removing Players

```java
// Remove specific player
audience.removePlayer(player1);

// Remove when player goes offline
@EventHandler
public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    
    // Remove from all relevant audiences
    getAllStagesForPlayer(player).forEach(stage -> {
        stage.getAudience().removePlayer(player);
    });
}
```

### Batch Operations

```java
// Add multiple players at once
Set<Player> newMembers = getNewGuildMembers();
newMembers.forEach(audience::addPlayer);

// Replace entire audience
Set<UUID> updatedMembers = getUpdatedMembersList();
Audience newAudience = Audience.fromUUIDs(updatedMembers);
stage.setAudience(newAudience); // If supported
```

## Audience Queries

### Checking Membership

```java
public boolean canPlayerSeeStage(Player player, Stage stage) {
    return stage.getAudience().contains(player);
}

public void validateAccess(Player player, Stage stage) {
    if (!stage.getAudience().contains(player)) {
        throw new SecurityException("Player not authorized to access this stage");
    }
}
```

### Getting Audience Information

```java
public void printAudienceInfo(Audience audience) {
    Set<Player> onlinePlayers = audience.getOnlinePlayers();
    int totalSize = audience.size();
    
    plugin.getLogger().info("Audience Info:");
    plugin.getLogger().info("  Online players: " + onlinePlayers.size());
    plugin.getLogger().info("  Total members: " + totalSize);
    plugin.getLogger().info("  Is empty: " + audience.isEmpty());
    
    // List online players
    onlinePlayers.forEach(player -> {
        plugin.getLogger().info("  - " + player.getName());
    });
}
```

## Advanced Audience Patterns

### Hierarchical Audiences

```java
public class HierarchicalAudience {
    private final Audience owners;      // Full access
    private final Audience members;     // View access
    private final Audience guests;      // Limited access
    
    public HierarchicalAudience() {
        this.owners = Audience.fromPlayers(new HashSet<>());
        this.members = Audience.fromPlayers(new HashSet<>());
        this.guests = Audience.fromPlayers(new HashSet<>());
    }
    
    public boolean canModify(Player player) {
        return owners.contains(player);
    }
    
    public boolean canView(Player player) {
        return owners.contains(player) || 
               members.contains(player) || 
               guests.contains(player);
    }
    
    public void promoteToMember(Player player) {
        if (guests.contains(player)) {
            guests.removePlayer(player);
            members.addPlayer(player);
        }
    }
    
    public Audience getCombinedAudience() {
        Set<Player> allPlayers = new HashSet<>();
        allPlayers.addAll(owners.getOnlinePlayers());
        allPlayers.addAll(members.getOnlinePlayers());
        allPlayers.addAll(guests.getOnlinePlayers());
        return Audience.fromPlayers(allPlayers);
    }
}
```

### Conditional Audiences

```java
public class ConditionalAudience {
    private final Set<UUID> baseMembers;
    private final Predicate<Player> condition;
    
    public ConditionalAudience(Set<UUID> baseMembers, Predicate<Player> condition) {
        this.baseMembers = baseMembers;
        this.condition = condition;
    }
    
    public Audience getCurrentAudience() {
        return baseMembers.stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .filter(condition)
            .collect(Collectors.collectingAndThen(
                Collectors.toSet(),
                Audience::fromPlayers
            ));
    }
    
    // Example conditions
    public static ConditionalAudience createTimeBasedAudience(Set<UUID> members, int startHour, int endHour) {
        return new ConditionalAudience(members, player -> {
            int currentHour = LocalTime.now().getHour();
            return currentHour >= startHour && currentHour < endHour;
        });
    }
    
    public static ConditionalAudience createLevelBasedAudience(Set<UUID> members, int minLevel) {
        return new ConditionalAudience(members, player -> player.getLevel() >= minLevel);
    }
}
```

### Audience Synchronization

```java
public class AudienceSynchronizer {
    private final Map<String, Set<UUID>> groupMembers = new ConcurrentHashMap<>();
    private final Map<String, Audience> stageAudiences = new ConcurrentHashMap<>();
    
    public void updateGroupMembers(String groupId, Set<UUID> newMembers) {
        Set<UUID> oldMembers = groupMembers.put(groupId, newMembers);
        
        // Find affected stages
        stageAudiences.entrySet().stream()
            .filter(entry -> entry.getKey().contains(groupId))
            .forEach(entry -> {
                String stageId = entry.getKey();
                Audience audience = entry.getValue();
                
                // Remove old members
                if (oldMembers != null) {
                    oldMembers.stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .forEach(audience::removePlayer);
                }
                
                // Add new members
                newMembers.stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(audience::addPlayer);
            });
    }
    
    public void syncAudience(String stageId, String groupId) {
        Set<UUID> members = groupMembers.get(groupId);
        if (members != null) {
            Audience audience = Audience.fromUUIDs(members);
            stageAudiences.put(stageId, audience);
        }
    }
}
```

## Common Use Cases

### Private Player Areas

```java
public Stage createPrivateArea(Player owner) {
    // Only the owner can see this area
    Audience privateAudience = Audience.fromPlayers(Set.of(owner));
    
    Stage privateStage = new Stage(
        "private_" + owner.getUniqueId(),
        owner.getWorld(),
        pos1, pos2,
        privateAudience
    );
    
    return privateStage;
}
```

### Guild/Team Areas

```java
public Stage createGuildHall(Guild guild) {
    // All guild members can see this area
    Set<UUID> guildMembers = guild.getMemberUUIDs();
    Audience guildAudience = Audience.fromUUIDs(guildMembers);
    
    Stage guildStage = new Stage(
        "guild_" + guild.getId(),
        guild.getWorld(),
        pos1, pos2,
        guildAudience
    );
    
    return guildStage;
}
```

### Public with Restrictions

```java
public Stage createRestrictedPublicArea(Location location) {
    // All online players with permission can see this
    Set<Player> authorizedPlayers = Bukkit.getOnlinePlayers().stream()
        .filter(player -> player.hasPermission("special.area.view"))
        .collect(Collectors.toSet());
    
    Audience restrictedAudience = Audience.fromPlayers(authorizedPlayers);
    
    return new Stage("restricted_public", location.getWorld(), pos1, pos2, restrictedAudience);
}
```

### Event-Based Areas

```java
public class EventAreaManager {
    
    public Stage createEventArea(String eventId, List<Player> participants) {
        Audience eventAudience = Audience.fromPlayers(new HashSet<>(participants));
        
        Stage eventStage = new Stage(
            "event_" + eventId,
            getEventWorld(),
            getEventPos1(), getEventPos2(),
            eventAudience
        );
        
        // Auto-cleanup after event
        scheduleCleanup(eventStage, Duration.ofHours(2));
        
        return eventStage;
    }
    
    public void addSpectators(Stage eventStage, List<Player> spectators) {
        Audience audience = eventStage.getAudience();
        spectators.forEach(audience::addPlayer);
    }
}
```

## Audience Persistence

### Saving Audience Data

```java
public class AudiencePersistence {
    
    public void saveAudience(String stageId, Audience audience) {
        Set<UUID> memberUUIDs = audience.getOnlinePlayers().stream()
            .map(Player::getUniqueId)
            .collect(Collectors.toSet());
        
        // Save to database or file
        ConfigurationSection config = plugin.getConfig().createSection("audiences." + stageId);
        config.set("members", memberUUIDs.stream()
            .map(UUID::toString)
            .collect(Collectors.toList()));
        config.set("saved_at", System.currentTimeMillis());
        
        plugin.saveConfig();
    }
    
    public Audience loadAudience(String stageId) {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("audiences." + stageId);
        if (config == null) {
            return Audience.fromPlayers(new HashSet<>());
        }
        
        List<String> memberStrings = config.getStringList("members");
        Set<UUID> memberUUIDs = memberStrings.stream()
            .map(UUID::fromString)
            .collect(Collectors.toSet());
        
        return Audience.fromUUIDs(memberUUIDs);
    }
}
```

### Database Integration

```java
public class DatabaseAudienceManager {
    private final HikariDataSource dataSource;
    
    public void saveAudience(String stageId, Audience audience) {
        try (Connection conn = dataSource.getConnection()) {
            // Clear existing members
            try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM stage_audiences WHERE stage_id = ?")) {
                stmt.setString(1, stageId);
                stmt.executeUpdate();
            }
            
            // Insert current members
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO stage_audiences (stage_id, player_uuid) VALUES (?, ?)")) {
                
                for (Player player : audience.getOnlinePlayers()) {
                    stmt.setString(1, stageId);
                    stmt.setString(2, player.getUniqueId().toString());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save audience", e);
        }
    }
    
    public Audience loadAudience(String stageId) {
        Set<UUID> memberUUIDs = new HashSet<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT player_uuid FROM stage_audiences WHERE stage_id = ?")) {
            
            stmt.setString(1, stageId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    memberUUIDs.add(UUID.fromString(rs.getString("player_uuid")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load audience", e);
        }
        
        return Audience.fromUUIDs(memberUUIDs);
    }
}
```

## Performance Optimization

### Audience Caching

```java
public class CachedAudienceManager {
    private final Map<String, Audience> audienceCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUpdate = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    
    public Audience getCachedAudience(String stageId) {
        long currentTime = System.currentTimeMillis();
        Long lastUpdateTime = lastUpdate.get(stageId);
        
        if (lastUpdateTime == null || currentTime - lastUpdateTime > CACHE_DURATION) {
            // Refresh cache
            Audience freshAudience = loadAudienceFromSource(stageId);
            audienceCache.put(stageId, freshAudience);
            lastUpdate.put(stageId, currentTime);
        }
        
        return audienceCache.get(stageId);
    }
    
    public void invalidateCache(String stageId) {
        audienceCache.remove(stageId);
        lastUpdate.remove(stageId);
    }
}
```

### Memory Efficient Audiences

```java
public class CompactAudience {
    private final THashSet<UUID> memberUUIDs; // Trove collection - more memory efficient
    
    public CompactAudience() {
        this.memberUUIDs = new THashSet<>();
    }
    
    public boolean contains(Player player) {
        return memberUUIDs.contains(player.getUniqueId());
    }
    
    public Set<Player> getOnlinePlayers() {
        return memberUUIDs.stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
    
    public int size() {
        return memberUUIDs.size();
    }
}
```

## Best Practices

### Audience Lifecycle Management

```java
public class AudienceLifecycleManager {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Add player to relevant audiences
        getRelevantStages(player).forEach(stage -> {
            if (shouldPlayerSeeStage(player, stage)) {
                stage.getAudience().addPlayer(player);
            }
        });
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Remove from all audiences to prevent memory leaks
        getAllStages().forEach(stage -> {
            stage.getAudience().removePlayer(player);
        });
    }
    
    @EventHandler
    public void onPermissionChange(PlayerPermissionChangeEvent event) {
        Player player = event.getPlayer();
        
        // Re-evaluate audience memberships
        reevaluatePlayerAudiences(player);
    }
}
```

### Error Handling

```java
public void safeAudienceOperation(Audience audience, Consumer<Audience> operation) {
    try {
        operation.accept(audience);
    } catch (Exception e) {
        plugin.getLogger().log(Level.WARNING, "Audience operation failed", e);
        
        // Attempt recovery
        if (audience.isEmpty()) {
            plugin.getLogger().info("Audience is empty, creating fallback");
            // Create fallback audience or handle gracefully
        }
    }
}
```

### Security Considerations

```java
public class SecureAudienceManager {
    
    public boolean canAddPlayerToAudience(Player admin, Player target, Stage stage) {
        // Check if admin has permission to modify this stage
        if (!stage.getId().startsWith("owned_by_" + admin.getUniqueId()) && 
            !admin.hasPermission("blocket.admin.manage")) {
            return false;
        }
        
        // Check if target player consents (if required)
        if (requiresConsent(stage) && !hasConsent(target, stage)) {
            return false;
        }
        
        return true;
    }
    
    public void auditAudienceChange(Player admin, Player target, Stage stage, String action) {
        auditLogger.log(String.format(
            "Audience %s: %s performed %s on %s for stage %s",
            action, admin.getName(), action, target.getName(), stage.getId()
        ));
    }
}
```

## Troubleshooting

### Common Issues

#### Players Not Seeing Blocks

```java
public void diagnoseVisibility(Player player, Stage stage) {
    Audience audience = stage.getAudience();
    
    plugin.getLogger().info("Visibility Diagnosis for " + player.getName() + ":");
    plugin.getLogger().info("  Stage ID: " + stage.getId());
    plugin.getLogger().info("  Player in audience: " + audience.contains(player));
    plugin.getLogger().info("  Audience size: " + audience.size());
    plugin.getLogger().info("  Online in audience: " + audience.getOnlinePlayers().size());
    
    if (!audience.contains(player)) {
        plugin.getLogger().info("  Solution: Add player to audience");
        audience.addPlayer(player);
        stage.sendBlocksToAudience();
    }
}
```

#### Memory Leaks

```java
public void detectMemoryLeaks() {
    getAllStages().forEach(stage -> {
        Audience audience = stage.getAudience();
        Set<Player> onlinePlayers = audience.getOnlinePlayers();
        int totalSize = audience.size();
        
        if (totalSize > onlinePlayers.size() * 2) {
            plugin.getLogger().warning(String.format(
                "Potential memory leak in stage %s: %d total members, %d online",
                stage.getId(), totalSize, onlinePlayers.size()
            ));
        }
    });
}
```

---

**Proper audience management is crucial for both functionality and performance. Use these patterns to create secure, efficient visibility systems!**
