# Installation

This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

This guide will help you integrate Blocket into your Minecraft plugin project.

## Requirements

Before installing Blocket, ensure your development environment meets these requirements:

- **Java**: 17 or higher
- **Minecraft Server**: Spigot/Paper 1.16+
- **Build Tool**: Maven or Gradle
- **Dependencies**: PacketEvents 2.0+ (automatically included)

## Maven Installation

### 1. Add Repository

Add the TWME repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>twme-repo-releases</id>
        <name>TWME Repository</name>
        <url>https://repo.twme.dev/releases</url>
    </repository>
</repositories>
```

### 2. Add Dependency

Add Blocket as a dependency:

```xml
<dependencies>
    <dependency>
        <groupId>dev.twme</groupId>
        <artifactId>blocket-api</artifactId>
        <version>1.0.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### 3. Shade Configuration (Optional)

If you want to include Blocket directly in your plugin JAR:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.4.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <relocations>
                            <relocation>
                                <pattern>dev.twme.blocket</pattern>
                                <shadedPattern>your.package.blocket</shadedPattern>
                            </relocation>
                        </relocations>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Gradle Installation

### 1. Add Repository

Add the repository to your `build.gradle`:

```gradle
repositories {
    maven {
        name = 'twme-repo'
        url = 'https://repo.twme.dev/releases'
    }
}
```

### 2. Add Dependency

Add Blocket as a dependency:

```gradle
dependencies {
    implementation 'dev.twme:blocket-api:1.0.0'
}
```

### 3. Shadow Configuration (Optional)

If using the Gradle Shadow plugin:

```gradle
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

shadowJar {
    relocate 'dev.twme.blocket', 'your.package.blocket'
}
```

## Plugin Setup

### 1. plugin.yml Configuration

Ensure your `plugin.yml` includes PacketEvents as a dependency:

```yaml
name: YourPlugin
version: 1.0.0
main: your.package.YourPlugin
api-version: 1.16

# PacketEvents is automatically included with Blocket
depend: []
softdepend: [PacketEvents]
```

### 2. Basic Plugin Structure

Create your main plugin class:

```java
package your.package;

import dev.twme.blocket.api.BlocketAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class YourPlugin extends JavaPlugin {
    private BlocketAPI blocketAPI;
    
    @Override
    public void onEnable() {
        // Initialize Blocket API
        blocketAPI = BlocketAPI.initialize(this);
        
        getLogger().info("Plugin enabled with Blocket support!");
        
        // Your plugin initialization code here...
    }
    
    @Override
    public void onDisable() {
        // Always shutdown Blocket API in onDisable
        if (blocketAPI != null) {
            blocketAPI.shutdown();
        }
        
        getLogger().info("Plugin disabled.");
    }
    
    public BlocketAPI getBlocketAPI() {
        return blocketAPI;
    }
}
```

## Verification

### Test Installation

Create a simple test to verify Blocket is working:

```java
@Override
public void onEnable() {
    blocketAPI = BlocketAPI.initialize(this);
    
    // Verify installation
    if (BlocketAPI.isInitialized()) {
        getLogger().info("✓ Blocket API initialized successfully!");
        getLogger().info("✓ Stage Manager: " + blocketAPI.getStageManager());
        getLogger().info("✓ Block Change Manager: " + blocketAPI.getBlockChangeManager());
    } else {
        getLogger().severe("✗ Failed to initialize Blocket API!");
        getServer().getPluginManager().disablePlugin(this);
    }
}
```

### Common Issues

#### PacketEvents Not Found

**Error**: `NoClassDefFoundError: PacketEvents`

**Solution**: Ensure PacketEvents is properly installed on your server. Blocket includes it as a dependency, but the server needs to load it.

#### Version Conflicts

**Error**: `NoSuchMethodError` or `ClassNotFoundException`

**Solution**: Check that you're using compatible versions:
- Blocket 1.0.0 requires PacketEvents 2.0+
- Ensure no conflicting versions in your classpath

#### Initialization Fails

**Error**: `IllegalStateException: BlocketAPI is already initialized`

**Solution**: Only call `BlocketAPI.initialize()` once per plugin. Check if you're calling it multiple times.

## Development Environment

### IDE Setup

For development with modern IDEs:

1. **IntelliJ IDEA**: Project should auto-detect dependencies
2. **Eclipse**: Ensure Maven/Gradle integration is enabled
3. **VSCode**: Install Java extension pack

### Debugging

Enable debug logging by adding this to your plugin:

```java
// In your onEnable method
if (getConfig().getBoolean("debug", false)) {
    getLogger().setLevel(java.util.logging.Level.FINE);
}
```

## Next Steps

After successful installation:

1. 📖 Read the [Quick Start](Quick-Start) guide
2. 🏗️ Learn about [Stages and Views](Stages-and-Views)
3. 🎯 Explore [Configuration](Configuration) options
4. 💡 Check out the [Examples](Example-Private-Mine)

---

**Need help?** Check the [troubleshooting section](Troubleshooting) or open an issue on GitHub.
