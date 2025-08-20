# Block Patterns
This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Master the pattern system to create complex and dynamic virtual block distributions in Blocket.

## Pattern Overview

**Patterns** define what types of blocks appear in your virtual areas and with what probability. They're the core of Blocket's block generation system.

### Basic Concept

A pattern is a map of `BlockData` to `Double` values, where:

- **Key**: The block type (e.g., `Material.STONE.createBlockData()`)
- **Value**: The probability weight (e.g., `70.0` for 70% chance)

```java
Map<BlockData, Double> pattern = new HashMap<>();
pattern.put(Material.STONE.createBlockData(), 70.0);  // 70% stone
pattern.put(Material.COAL_ORE.createBlockData(), 30.0); // 30% coal ore
```

## Creating Patterns

### Simple Ore Pattern

```java
import dev.twme.blocket.models.Pattern;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public Pattern createSimpleOrePattern() {
    Map<BlockData, Double> blocks = new HashMap<>();
    
    // Common blocks (higher probability)
    blocks.put(Material.STONE.createBlockData(), 60.0);
    blocks.put(Material.COAL_ORE.createBlockData(), 25.0);
    
    // Uncommon blocks
    blocks.put(Material.IRON_ORE.createBlockData(), 12.0);
    
    // Rare blocks (lower probability)
    blocks.put(Material.DIAMOND_ORE.createBlockData(), 3.0);
    
    return new Pattern(blocks);
}
```

### Advanced Patterns with Block States

```java
public Pattern createAdvancedFarmPattern() {
    Map<BlockData, Double> crops = new HashMap<>();
    
    // Different growth stages of wheat
    BlockData wheatStage0 = Material.WHEAT.createBlockData();
    BlockData wheatStage7 = Material.WHEAT.createBlockData();
    if (wheatStage7 instanceof Ageable ageable) {
        ageable.setAge(7); // Fully grown
    }
    
    crops.put(wheatStage0, 30.0);  // Young crops
    crops.put(wheatStage7, 70.0);  // Mature crops
    
    return new Pattern(crops);
}
```

### Weighted Distribution Examples

#### Mining Pattern (Conservative)

```java
public Pattern createConservativeMiningPattern() {
    Map<BlockData, Double> blocks = new HashMap<>();
    
    blocks.put(Material.STONE.createBlockData(), 75.0);        // Very common
    blocks.put(Material.COAL_ORE.createBlockData(), 15.0);     // Common
    blocks.put(Material.IRON_ORE.createBlockData(), 7.0);      // Uncommon
    blocks.put(Material.GOLD_ORE.createBlockData(), 2.5);      // Rare
    blocks.put(Material.DIAMOND_ORE.createBlockData(), 0.5);   // Very rare
    
    return new Pattern(blocks);
}
```

#### Mining Pattern (Generous)

```java
public Pattern createGenerousMiningPattern() {
    Map<BlockData, Double> blocks = new HashMap<>();
    
    blocks.put(Material.STONE.createBlockData(), 40.0);        // Less common
    blocks.put(Material.COAL_ORE.createBlockData(), 25.0);     // More common
    blocks.put(Material.IRON_ORE.createBlockData(), 20.0);     // Much more common
    blocks.put(Material.GOLD_ORE.createBlockData(), 10.0);     // Common
    blocks.put(Material.DIAMOND_ORE.createBlockData(), 5.0);   // Uncommon
    
    return new Pattern(blocks);
}
```

#### Decorative Pattern

```java
public Pattern createDecorativePattern() {
    Map<BlockData, Double> blocks = new HashMap<>();
    
    // Various decorative blocks
    blocks.put(Material.POLISHED_GRANITE.createBlockData(), 25.0);
    blocks.put(Material.POLISHED_DIORITE.createBlockData(), 25.0);
    blocks.put(Material.POLISHED_ANDESITE.createBlockData(), 25.0);
    blocks.put(Material.SMOOTH_STONE.createBlockData(), 25.0);
    
    return new Pattern(blocks);
}
```

## Working with Complex Block Data

### Directional Blocks

```java
public Pattern createDirectionalPattern() {
    Map<BlockData, Double> blocks = new HashMap<>();
    
    // Logs with different orientations
    BlockData verticalLog = Material.OAK_LOG.createBlockData();
    BlockData horizontalLogX = Material.OAK_LOG.createBlockData();
    BlockData horizontalLogZ = Material.OAK_LOG.createBlockData();
    
    if (horizontalLogX instanceof Orientable orientableX) {
        orientableX.setAxis(Axis.X);
    }
    if (horizontalLogZ instanceof Orientable orientableZ) {
        orientableZ.setAxis(Axis.Z);
    }
    
    blocks.put(verticalLog, 60.0);      // Vertical logs more common
    blocks.put(horizontalLogX, 20.0);   // Horizontal logs
    blocks.put(horizontalLogZ, 20.0);
    
    return new Pattern(blocks);
}
```

### Age-Based Blocks

```java
public Pattern createCropPattern() {
    Map<BlockData, Double> crops = new HashMap<>();
    
    // Different growth stages with different probabilities
    for (int age = 0; age <= 7; age++) {
        BlockData wheat = Material.WHEAT.createBlockData();
        if (wheat instanceof Ageable ageable) {
            ageable.setAge(age);
        }
        
        // Higher probability for more mature crops
        double probability = (age + 1) * 2.0; // Age 0 = 2%, Age 7 = 16%
        crops.put(wheat, probability);
    }
    
    return new Pattern(crops);
}
```

### Waterlogged Blocks

```java
public Pattern createUnderwaterPattern() {
    Map<BlockData, Double> blocks = new HashMap<>();
    
    // Regular and waterlogged versions
    BlockData regularStone = Material.STONE.createBlockData();
    BlockData waterloggedSlab = Material.STONE_SLAB.createBlockData();
    
    if (waterloggedSlab instanceof Waterlogged waterlogged) {
        waterlogged.setWaterlogged(true);
    }
    
    blocks.put(regularStone, 70.0);
    blocks.put(waterloggedSlab, 30.0);
    
    return new Pattern(blocks);
}
```

## Dynamic Pattern Generation

### Level-Based Patterns

```java
public Pattern createLevelBasedPattern(int playerLevel) {
    Map<BlockData, Double> blocks = new HashMap<>();
    
    // Base blocks always present
    blocks.put(Material.STONE.createBlockData(), 60.0);
    blocks.put(Material.COAL_ORE.createBlockData(), 25.0);
    
    // Additional ores based on level
    if (playerLevel >= 10) {
        blocks.put(Material.IRON_ORE.createBlockData(), 12.0);
    }
    if (playerLevel >= 25) {
        blocks.put(Material.GOLD_ORE.createBlockData(), 5.0);
    }
    if (playerLevel >= 50) {
        blocks.put(Material.DIAMOND_ORE.createBlockData(), 3.0);
    }
    if (playerLevel >= 75) {
        blocks.put(Material.EMERALD_ORE.createBlockData(), 1.0);
    }
    
    // Normalize probabilities to ensure they add up correctly
    return normalizePattern(blocks);
}

private Pattern normalizePattern(Map<BlockData, Double> blocks) {
    double total = blocks.values().stream().mapToDouble(Double::doubleValue).sum();
    
    Map<BlockData, Double> normalized = new HashMap<>();
    blocks.forEach((block, weight) -> {
        normalized.put(block, (weight / total) * 100.0);
    });
    
    return new Pattern(normalized);
}
```

### Time-Based Patterns

```java
public Pattern createTimeBasedPattern() {
    Map<BlockData, Double> blocks = new HashMap<>();
    
    long currentTime = System.currentTimeMillis();
    boolean isDay = (currentTime / 24000) % 2 == 0; // Simplified day/night cycle
    
    blocks.put(Material.STONE.createBlockData(), 70.0);
    
    if (isDay) {
        // Day ores
        blocks.put(Material.IRON_ORE.createBlockData(), 20.0);
        blocks.put(Material.GOLD_ORE.createBlockData(), 10.0);
    } else {
        // Night ores (more valuable)
        blocks.put(Material.DIAMOND_ORE.createBlockData(), 15.0);
        blocks.put(Material.EMERALD_ORE.createBlockData(), 15.0);
    }
    
    return new Pattern(blocks);
}
```

### Biome-Specific Patterns

```java
public Pattern createBiomePattern(Biome biome) {
    Map<BlockData, Double> blocks = new HashMap<>();
    
    switch (biome) {
        case DESERT:
            blocks.put(Material.SANDSTONE.createBlockData(), 60.0);
            blocks.put(Material.GOLD_ORE.createBlockData(), 30.0);
            blocks.put(Material.LAPIS_ORE.createBlockData(), 10.0);
            break;
            
        case MOUNTAINS:
            blocks.put(Material.STONE.createBlockData(), 50.0);
            blocks.put(Material.IRON_ORE.createBlockData(), 25.0);
            blocks.put(Material.COAL_ORE.createBlockData(), 20.0);
            blocks.put(Material.EMERALD_ORE.createBlockData(), 5.0);
            break;
            
        case OCEAN:
            blocks.put(Material.PRISMARINE.createBlockData(), 40.0);
            blocks.put(Material.SEA_LANTERN.createBlockData(), 35.0);
            blocks.put(Material.SPONGE.createBlockData(), 25.0);
            break;
            
        default:
            // Fallback pattern
            blocks.put(Material.STONE.createBlockData(), 70.0);
            blocks.put(Material.COAL_ORE.createBlockData(), 30.0);
    }
    
    return new Pattern(blocks);
}
```

## Pattern Validation

### Ensuring Valid Probabilities

```java
public class PatternValidator {
    
    public static boolean isValidPattern(Map<BlockData, Double> blocks) {
        // Check for null or empty
        if (blocks == null || blocks.isEmpty()) {
            return false;
        }
        
        // Check for negative probabilities
        boolean hasNegative = blocks.values().stream()
            .anyMatch(weight -> weight < 0);
        if (hasNegative) {
            return false;
        }
        
        // Check total probability (should be > 0)
        double total = blocks.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        
        return total > 0;
    }
    
    public static Pattern createSafePattern(Map<BlockData, Double> blocks) {
        if (!isValidPattern(blocks)) {
            // Fallback to default pattern
            Map<BlockData, Double> fallback = new HashMap<>();
            fallback.put(Material.STONE.createBlockData(), 100.0);
            return new Pattern(fallback);
        }
        
        return new Pattern(blocks);
    }
}
```

### Pattern Debugging

```java
public class PatternDebugger {
    
    public static void printPattern(Pattern pattern, String name) {
        System.out.println("=== Pattern: " + name + " ===");
        
        // Get pattern blocks through reflection or custom getter
        Map<BlockData, Double> blocks = getPatternBlocks(pattern);
        
        double total = blocks.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        
        blocks.entrySet().stream()
            .sorted(Map.Entry.<BlockData, Double>comparingByValue().reversed())
            .forEach(entry -> {
                BlockData block = entry.getKey();
                Double weight = entry.getValue();
                double percentage = (weight / total) * 100.0;
                
                System.out.printf("  %s: %.1f%% (weight: %.1f)%n",
                    block.getMaterial().name(),
                    percentage,
                    weight
                );
            });
        
        System.out.println("  Total weight: " + total);
        System.out.println();
    }
}
```

## Advanced Pattern Techniques

### Conditional Patterns

```java
public class ConditionalPattern {
    
    public Pattern createConditionalPattern(Player player, BlocketPosition position) {
        Map<BlockData, Double> blocks = new HashMap<>();
        
        // Base pattern
        blocks.put(Material.STONE.createBlockData(), 60.0);
        
        // Add blocks based on conditions
        if (player.hasPermission("mine.rare")) {
            blocks.put(Material.DIAMOND_ORE.createBlockData(), 10.0);
        }
        
        if (position.getY() < 32) {
            // Deep underground - more valuable ores
            blocks.put(Material.REDSTONE_ORE.createBlockData(), 20.0);
        } else {
            // Surface level - common ores
            blocks.put(Material.COAL_ORE.createBlockData(), 30.0);
        }
        
        return new Pattern(blocks);
    }
}
```

### Pattern Inheritance

```java
public class PatternBuilder {
    private Map<BlockData, Double> blocks = new HashMap<>();
    
    public PatternBuilder extend(Pattern basePattern) {
        // Add blocks from base pattern
        Map<BlockData, Double> baseBlocks = getPatternBlocks(basePattern);
        this.blocks.putAll(baseBlocks);
        return this;
    }
    
    public PatternBuilder add(Material material, double weight) {
        blocks.put(material.createBlockData(), weight);
        return this;
    }
    
    public PatternBuilder remove(Material material) {
        blocks.remove(material.createBlockData());
        return this;
    }
    
    public PatternBuilder multiply(Material material, double factor) {
        BlockData blockData = material.createBlockData();
        if (blocks.containsKey(blockData)) {
            blocks.put(blockData, blocks.get(blockData) * factor);
        }
        return this;
    }
    
    public Pattern build() {
        return new Pattern(new HashMap<>(blocks));
    }
}

// Usage:
Pattern baseOrePattern = createSimpleOrePattern();
Pattern enhancedPattern = new PatternBuilder()
    .extend(baseOrePattern)
    .add(Material.EMERALD_ORE, 2.0)
    .multiply(Material.DIAMOND_ORE, 2.0) // Double diamond chance
    .build();
```

### Pattern Combinations

```java
public Pattern combinePatterns(Pattern pattern1, Pattern pattern2, double ratio) {
    Map<BlockData, Double> combined = new HashMap<>();
    
    Map<BlockData, Double> blocks1 = getPatternBlocks(pattern1);
    Map<BlockData, Double> blocks2 = getPatternBlocks(pattern2);
    
    // Add blocks from first pattern with ratio weighting
    blocks1.forEach((block, weight) -> {
        combined.put(block, weight * ratio);
    });
    
    // Add blocks from second pattern with inverse ratio weighting
    blocks2.forEach((block, weight) -> {
        combined.put(block, combined.getOrDefault(block, 0.0) + weight * (1.0 - ratio));
    });
    
    return new Pattern(combined);
}

// Usage:
Pattern orePattern = createOrePattern();
Pattern gemPattern = createGemPattern();
Pattern combined = combinePatterns(orePattern, gemPattern, 0.7); // 70% ore, 30% gem
```

## Performance Optimization

### Efficient Pattern Creation

```java
// Good: Reuse patterns
private static final Pattern COMMON_ORE_PATTERN = createCommonOrePattern();
private static final Pattern RARE_ORE_PATTERN = createRareOrePattern();

public Pattern getPatternForPlayer(Player player) {
    return player.hasPermission("mine.rare") ? RARE_ORE_PATTERN : COMMON_ORE_PATTERN;
}

// Avoid: Creating patterns repeatedly
public Pattern getPatternForPlayer(Player player) {
    // This creates new objects every time - inefficient!
    Map<BlockData, Double> blocks = new HashMap<>();
    blocks.put(Material.STONE.createBlockData(), 70.0);
    // ... rest of pattern
    return new Pattern(blocks);
}
```

### Pattern Caching

```java
public class PatternCache {
    private static final Map<String, Pattern> CACHE = new ConcurrentHashMap<>();
    
    public static Pattern getCachedPattern(String key, Supplier<Pattern> creator) {
        return CACHE.computeIfAbsent(key, k -> creator.get());
    }
    
    public static void clearCache() {
        CACHE.clear();
    }
}

// Usage:
Pattern levelPattern = PatternCache.getCachedPattern(
    "level_" + playerLevel,
    () -> createLevelBasedPattern(playerLevel)
);
```

## Common Patterns Library

Here are some ready-to-use patterns for common scenarios:

### Mining Patterns

```java
public class MiningPatterns {
    
    public static Pattern BEGINNER_MINE() {
        Map<BlockData, Double> blocks = new HashMap<>();
        blocks.put(Material.STONE.createBlockData(), 80.0);
        blocks.put(Material.COAL_ORE.createBlockData(), 20.0);
        return new Pattern(blocks);
    }
    
    public static Pattern INTERMEDIATE_MINE() {
        Map<BlockData, Double> blocks = new HashMap<>();
        blocks.put(Material.STONE.createBlockData(), 65.0);
        blocks.put(Material.COAL_ORE.createBlockData(), 20.0);
        blocks.put(Material.IRON_ORE.createBlockData(), 12.0);
        blocks.put(Material.GOLD_ORE.createBlockData(), 3.0);
        return new Pattern(blocks);
    }
    
    public static Pattern EXPERT_MINE() {
        Map<BlockData, Double> blocks = new HashMap<>();
        blocks.put(Material.STONE.createBlockData(), 45.0);
        blocks.put(Material.COAL_ORE.createBlockData(), 25.0);
        blocks.put(Material.IRON_ORE.createBlockData(), 15.0);
        blocks.put(Material.GOLD_ORE.createBlockData(), 8.0);
        blocks.put(Material.DIAMOND_ORE.createBlockData(), 5.0);
        blocks.put(Material.EMERALD_ORE.createBlockData(), 2.0);
        return new Pattern(blocks);
    }
}
```

### Farm Patterns

```java
public class FarmPatterns {
    
    public static Pattern WHEAT_FARM() {
        Map<BlockData, Double> blocks = new HashMap<>();
        
        // Different growth stages
        for (int age = 0; age <= 7; age++) {
            BlockData wheat = Material.WHEAT.createBlockData();
            if (wheat instanceof Ageable ageable) {
                ageable.setAge(age);
            }
            blocks.put(wheat, age < 6 ? 5.0 : 35.0); // Favor mature crops
        }
        
        return new Pattern(blocks);
    }
    
    public static Pattern MIXED_CROPS() {
        Map<BlockData, Double> blocks = new HashMap<>();
        
        blocks.put(Material.WHEAT.createBlockData(), 30.0);
        blocks.put(Material.CARROTS.createBlockData(), 25.0);
        blocks.put(Material.POTATOES.createBlockData(), 25.0);
        blocks.put(Material.BEETROOTS.createBlockData(), 20.0);
        
        return new Pattern(blocks);
    }
}
```

## Best Practices

### 1. Keep Patterns Simple

```java
// Good: Clear and maintainable
Map<BlockData, Double> simplePattern = Map.of(
    Material.STONE.createBlockData(), 70.0,
    Material.COAL_ORE.createBlockData(), 30.0
);

// Avoid: Overly complex patterns
// (20+ different blocks with tiny probabilities)
```

### 2. Use Meaningful Names

```java
// Good: Descriptive names
Pattern IRON_AGE_MINING = createIronAgeMining();
Pattern SURFACE_DECORATIONS = createSurfaceDecorations();

// Avoid: Generic names
Pattern PATTERN1 = createPattern1();
Pattern STUFF = createStuff();
```

### 3. Validate Input

```java
public Pattern createSafePattern(Map<BlockData, Double> input) {
    if (input == null || input.isEmpty()) {
        return getDefaultPattern();
    }
    
    // Remove invalid entries
    Map<BlockData, Double> validated = input.entrySet().stream()
        .filter(entry -> entry.getValue() > 0)
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
        ));
    
    return validated.isEmpty() ? getDefaultPattern() : new Pattern(validated);
}
```

### 4. Consider Player Experience

```java
// Progressive difficulty
public Pattern getProgressivePattern(int stageLevel) {
    return switch (stageLevel) {
        case 1 -> MiningPatterns.BEGINNER_MINE();
        case 2, 3 -> MiningPatterns.INTERMEDIATE_MINE();
        case 4, 5 -> MiningPatterns.EXPERT_MINE();
        default -> createCustomPattern(stageLevel);
    };
}
```

## Troubleshooting

### Common Issues

#### No Blocks Appearing

**Problem**: Empty or zero-weight pattern
**Solution**: Ensure all weights are positive

```java
// Check pattern validity
Map<BlockData, Double> blocks = new HashMap<>();
blocks.put(Material.STONE.createBlockData(), 0.0); // BAD: Zero weight

// Fix:
blocks.put(Material.STONE.createBlockData(), 100.0); // GOOD: Positive weight
```

#### Unexpected Block Distribution

**Problem**: Weights don't add up as expected
**Solution**: Understand that weights are relative, not absolute percentages

```java
// These both create the same distribution (50/50):
Pattern pattern1 = new Pattern(Map.of(
    Material.STONE.createBlockData(), 50.0,
    Material.COAL_ORE.createBlockData(), 50.0
));

Pattern pattern2 = new Pattern(Map.of(
    Material.STONE.createBlockData(), 1.0,
    Material.COAL_ORE.createBlockData(), 1.0
));
```

## Next Steps

- 🎭 Learn about [Dynamic Block Management](Dynamic-Block-Management)
- 👥 Understand [Audience Management](Audience-Management)
- 📖 Check the [BlocketAPI Reference](BlocketAPI-Reference)
- 🎮 See patterns in action with [Examples](Example-Private-Mine)

---

**Mastering patterns is key to creating engaging and balanced virtual block systems!**
