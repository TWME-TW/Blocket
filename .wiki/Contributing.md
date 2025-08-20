# Contributing to Blocket
This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Welcome contributors! This guide helps you contribute to the Blocket project.

## Getting Started

### Development Environment Setup

1. **Prerequisites**:
   - Java 17 or higher
   - Maven 3.6+ or Gradle 7.0+
   - Git
   - IDE (IntelliJ IDEA recommended)
   - Minecraft server for testing (Paper/Spigot 1.16+)

2. **Fork and Clone**:
   ```bash
   # Fork the repository on GitHub
   git clone https://github.com/your-username/blocket.git
   cd blocket
   ```

3. **Build the Project**:
   ```bash
   # Using Maven
   mvn clean compile
   
   # Using Gradle
   ./gradlew build
   ```

4. **Setup IDE**:
   ```java
   // Import project into your IDE
   // Ensure Java 17 is configured
   // Install checkstyle plugin for code formatting
   ```

### Development Server Setup

Create a test server for development:

```bash
# Create test server directory
mkdir test-server
cd test-server

# Download Paper (recommended)
wget https://papermc.io/api/v2/projects/paper/versions/1.20.1/builds/196/downloads/paper-1.20.1-196.jar -O server.jar

# Create start script
echo "java -Xmx2G -Xms1G -jar server.jar --nogui" > start.sh
chmod +x start.sh

# Accept EULA
echo "eula=true" > eula.txt

# Install PacketEvents dependency
mkdir plugins
# Download PacketEvents to plugins folder
```

## Code Style and Standards

### Code Formatting

We use a consistent code style across the project:

```java
/**
 * Class documentation follows JavaDoc standards.
 * 
 * @author Your Name
 * @since 1.0.0
 */
public class ExampleClass {
    
    // Constants in UPPER_SNAKE_CASE
    private static final int MAX_AUDIENCE_SIZE = 1000;
    private static final String DEFAULT_STAGE_PREFIX = "stage_";
    
    // Fields in camelCase
    private final StageManager stageManager;
    private final Map<String, Stage> stageCache;
    
    /**
     * Constructor documentation.
     * 
     * @param stageManager The stage manager instance
     */
    public ExampleClass(StageManager stageManager) {
        this.stageManager = Objects.requireNonNull(stageManager, "stageManager cannot be null");
        this.stageCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Method documentation explaining purpose and parameters.
     * 
     * @param stageId The unique identifier for the stage
     * @param player The player to add to the stage
     * @return true if the player was added successfully
     * @throws IllegalArgumentException if stageId is null or empty
     */
    public boolean addPlayerToStage(String stageId, Player player) {
        if (stageId == null || stageId.isEmpty()) {
            throw new IllegalArgumentException("Stage ID cannot be null or empty");
        }
        
        Objects.requireNonNull(player, "Player cannot be null");
        
        Stage stage = stageCache.get(stageId);
        if (stage == null) {
            return false;
        }
        
        // Clear, concise logic with good variable names
        Audience audience = stage.getAudience();
        audience.addPlayer(player);
        
        return true;
    }
}
```

### Naming Conventions

- **Classes**: `PascalCase` (e.g., `StageManager`, `BlocketAPI`)
- **Methods**: `camelCase` (e.g., `addPlayer`, `getStageById`)
- **Variables**: `camelCase` (e.g., `stageId`, `playerList`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_STAGES`, `DEFAULT_CONFIG`)
- **Packages**: `lowercase` (e.g., `api`, `managers`, `events`)

### Documentation Standards

Every public class and method must have JavaDoc:

```java
/**
 * Manages virtual block stages and their lifecycle.
 * 
 * <p>The StageManager is responsible for creating, updating, and destroying
 * stages. It maintains thread-safe access to all stages and provides
 * efficient lookup operations.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * StageManager manager = BlocketAPI.getInstance().getStageManager();
 * Stage stage = new Stage("myStage", world, pos1, pos2, audience);
 * manager.addStage(stage);
 * }</pre>
 * 
 * @author Blocket Team
 * @since 1.0.0
 * @see Stage
 * @see BlocketAPI
 */
public class StageManager {
    
    /**
     * Creates a new stage with the specified parameters.
     * 
     * <p>The stage will be registered and immediately available for use.
     * If a stage with the same ID already exists, this method will
     * throw an IllegalArgumentException.</p>
     * 
     * @param stageId unique identifier for the stage, cannot be null
     * @param world the world where this stage exists, cannot be null
     * @param pos1 first corner of the stage boundary, cannot be null
     * @param pos2 second corner of the stage boundary, cannot be null
     * @param audience initial audience for this stage, cannot be null
     * @return the created stage
     * @throws IllegalArgumentException if stageId already exists
     * @throws NullPointerException if any parameter is null
     * @since 1.0.0
     */
    public Stage createStage(String stageId, World world, 
                           BlocketPosition pos1, BlocketPosition pos2, 
                           Audience audience) {
        // Implementation...
    }
}
```

## Contributing Guidelines

### Issue Reporting

Before reporting issues:

1. **Search existing issues** to avoid duplicates
2. **Test with latest version** of Blocket
3. **Gather system information**:
   - Blocket version
   - Server version (Paper/Spigot)
   - Java version
   - PacketEvents version
   - Other relevant plugins

**Issue Template**:

```markdown
### Bug Description
Clear description of the issue.

### Reproduction Steps
1. First step
2. Second step
3. Issue occurs

### Expected Behavior
What should happen instead.

### Environment
- Blocket version: 1.0.0
- Server: Paper 1.20.1
- Java: 17.0.2
- PacketEvents: 2.0.0

### Error Logs
```
Paste error logs here
```

### Additional Context
Any other relevant information.
```

### Feature Requests

When requesting features:

1. **Explain the use case** - why is this needed?
2. **Provide examples** - how would this be used?
3. **Consider alternatives** - are there existing solutions?
4. **Estimate impact** - how many users would benefit?

**Feature Template**:

```markdown
### Feature Summary
Brief description of the proposed feature.

### Use Case
Detailed explanation of why this feature is needed.
Include real-world scenarios where this would be useful.

### Proposed Implementation
How do you envision this feature working?
```java
// Example API usage
BlocketAPI api = BlocketAPI.getInstance();
api.newFeature().doSomething();
```

### Alternatives Considered
What other approaches have you considered?

### Impact Assessment
- Who would use this feature?
- How often would it be used?
- Any performance considerations?
```

## Development Workflow

### Branching Strategy

We use GitFlow for development:

- `main` - Production ready code
- `develop` - Integration branch for features
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Critical production fixes

### Making Changes

1. **Create a feature branch**:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**:
   - Follow code style guidelines
   - Add appropriate tests
   - Update documentation
   - Run tests locally

3. **Commit your changes**:
   ```bash
   # Use conventional commit messages
   git add .
   git commit -m "feat: add new stage management feature"
   
   # Or for bug fixes
   git commit -m "fix: resolve audience synchronization issue"
   ```

### Commit Message Format

We follow [Conventional Commits](https://conventionalcommits.org/):

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Types**:
- `feat` - New features
- `fix` - Bug fixes
- `docs` - Documentation changes
- `style` - Code formatting (no logic changes)
- `refactor` - Code refactoring
- `test` - Adding or updating tests
- `chore` - Maintenance tasks

**Examples**:
```bash
feat(api): add batch block update functionality
fix(events): prevent memory leak in audience management
docs: update installation guide for new repository
test: add unit tests for pattern generation
```

### Testing Requirements

All contributions must include appropriate tests:

```java
/**
 * Test class following naming convention: ClassNameTest
 */
public class StageManagerTest {
    
    private StageManager stageManager;
    private World testWorld;
    
    @BeforeEach
    void setUp() {
        // Setup test environment
        stageManager = new StageManager();
        testWorld = mock(World.class);
        when(testWorld.getName()).thenReturn("test_world");
    }
    
    @Test
    @DisplayName("Should create stage with valid parameters")
    void shouldCreateStageWithValidParameters() {
        // Given
        String stageId = "test_stage";
        BlocketPosition pos1 = new BlocketPosition(0, 0, 0);
        BlocketPosition pos2 = new BlocketPosition(10, 10, 10);
        Audience audience = Audience.fromPlayers(new HashSet<>());
        
        // When
        Stage result = stageManager.createStage(stageId, testWorld, pos1, pos2, audience);
        
        // Then
        assertNotNull(result);
        assertEquals(stageId, result.getId());
        assertEquals(testWorld, result.getWorld());
        assertTrue(stageManager.hasStage(stageId));
    }
    
    @Test
    @DisplayName("Should throw exception when creating duplicate stage")
    void shouldThrowExceptionWhenCreatingDuplicateStage() {
        // Given
        String stageId = "duplicate_stage";
        BlocketPosition pos1 = new BlocketPosition(0, 0, 0);
        BlocketPosition pos2 = new BlocketPosition(10, 10, 10);
        Audience audience = Audience.fromPlayers(new HashSet<>());
        
        stageManager.createStage(stageId, testWorld, pos1, pos2, audience);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            stageManager.createStage(stageId, testWorld, pos1, pos2, audience);
        });
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    @DisplayName("Should reject invalid stage IDs")
    void shouldRejectInvalidStageIds(String invalidId) {
        // Given
        BlocketPosition pos1 = new BlocketPosition(0, 0, 0);
        BlocketPosition pos2 = new BlocketPosition(10, 10, 10);
        Audience audience = Audience.fromPlayers(new HashSet<>());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            stageManager.createStage(invalidId, testWorld, pos1, pos2, audience);
        });
    }
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=StageManagerTest

# Run with coverage
mvn test jacoco:report

# Generate coverage report
mvn jacoco:report
```

## Code Review Process

### Pull Request Guidelines

1. **PR Title**: Follow conventional commit format
2. **Description**: Explain what and why, not just how
3. **Testing**: Describe how you tested the changes
4. **Screenshots**: Include if UI changes are involved
5. **Breaking Changes**: Clearly document any breaking changes

**PR Template**:

```markdown
## Description
Brief description of the changes and why they're needed.

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Testing
Describe the tests you ran and how to reproduce them:

- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing performed
- [ ] Tested on development server

## Checklist
- [ ] My code follows the project's style guidelines
- [ ] I have performed a self-review of my code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing unit tests pass locally with my changes

## Breaking Changes
List any breaking changes and migration guide if applicable.
```

### Review Criteria

Reviewers will check for:

1. **Functionality** - Does it work correctly?
2. **Code Quality** - Is it well-written and maintainable?
3. **Performance** - Are there any performance concerns?
4. **Security** - Are there any security implications?
5. **Testing** - Are there adequate tests?
6. **Documentation** - Is it properly documented?

## Documentation Contributions

### Wiki Updates

The wiki is crucial for users:

1. **Keep it current** - Update when features change
2. **Include examples** - Always provide working code examples
3. **Test examples** - Ensure all code examples actually work
4. **Link appropriately** - Create helpful cross-references

### API Documentation

Update JavaDoc when making API changes:

```java
/**
 * @deprecated since 1.2.0, will be removed in 2.0.0
 * Use {@link #newMethod()} instead.
 */
@Deprecated
public void oldMethod() {
    // Implementation
}

/**
 * New method that replaces the deprecated oldMethod.
 * 
 * <p>This method provides better performance and cleaner API.</p>
 * 
 * @since 1.2.0
 */
public void newMethod() {
    // Implementation
}
```

## Release Process

### Version Numbering

We use [Semantic Versioning](https://semver.org/):

- `MAJOR.MINOR.PATCH` (e.g., 1.2.3)
- `MAJOR` - Breaking changes
- `MINOR` - New features (backward compatible)
- `PATCH` - Bug fixes (backward compatible)

### Pre-release Checklist

Before releasing:

- [ ] All tests pass
- [ ] Documentation is updated
- [ ] CHANGELOG is updated
- [ ] Version numbers are updated
- [ ] Release notes are prepared
- [ ] Backward compatibility is verified

### Release Notes Format

```markdown
# Version 1.2.0

## Features
- **Stage Management**: Added batch stage operations for improved performance
- **Pattern System**: New `RandomPattern` for procedural generation
- **API Enhancement**: Builder pattern for `BlocketConfig`

## Bug Fixes
- Fixed memory leak in audience management (#123)
- Resolved chunk loading issues on Paper servers (#145)
- Fixed race condition in event handling (#156)

## Performance Improvements
- 40% faster block updates in large stages
- Reduced memory usage by 25% for audiences
- Optimized chunk loading for better server performance

## Breaking Changes
- `StageManager.createStage()` now requires `Audience` parameter
- `BlocketEvent` hierarchy has been refactored
- Configuration is now code-based instead of YAML

## Migration Guide
See [Migration Guide](Migration-Guide.md) for detailed upgrade instructions.

## Dependencies
- Minimum Java version: 17
- PacketEvents: 2.0.0+
- Bukkit/Spigot: 1.16.5+
```

## Community

### Getting Help

- **Discord**: Join our [Discord server](https://discord.gg/example) for real-time help
- **GitHub Discussions**: Use for general questions and ideas
- **GitHub Issues**: For bug reports and feature requests
- **Wiki**: Comprehensive documentation and guides

### Code of Conduct

We follow a simple code of conduct:

1. **Be respectful** - Treat everyone with respect
2. **Be constructive** - Provide helpful feedback
3. **Be patient** - Remember we're all volunteers
4. **Be inclusive** - Welcome newcomers and diverse perspectives

### Recognition

We appreciate all contributions:

- **Contributors file** - All contributors are listed
- **Release notes** - Significant contributions are highlighted
- **GitHub badges** - Show your contributor status

Thank you for contributing to Blocket! Your efforts help make virtual blocks better for everyone.

---

**Ready to contribute? Start by checking our [good first issues](https://github.com/your-org/blocket/labels/good%20first%20issue)!**
