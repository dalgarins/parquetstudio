# Plugin Development Guide

This guide covers development setup, architecture, and best practices for working on Parquet Studio.

## Development Environment

### Prerequisites

- **JDK 17+** - Required for compilation
- **IntelliJ IDEA 2023.3+** - IDE and plugin development
- **Gradle 7.6.3** - Build tool (included via wrapper)

### Initial Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/jhordyhuaman/parquet-studio.git
   cd parquet-studio
   ```

2. **Open in IntelliJ IDEA**:
   - File → Open → Select project directory
   - IntelliJ will detect the Gradle project

3. **Sync Gradle**:
   - IntelliJ should auto-sync
   - Or: View → Tool Windows → Gradle → Refresh

4. **Verify setup**:
   ```bash
   ./gradlew clean build
   ```

## Project Structure

```
parquet-studio/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/github/jhordyhuaman/parquetstudio/
│   │   │       ├── DuckDBParquetService.java
│   │   │       ├── ParquetData.java
│   │   │       ├── ParquetTableModel.java
│   │   │       ├── ParquetToolWindow.java
│   │   │       └── ParquetToolWindowFactory.java
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml
│   │       └── icons/
│   │           └── parquet_studio.svg
│   └── test/
│       └── java/
├── build.gradle.kts          # Build configuration
├── gradle.properties         # Plugin properties
├── settings.gradle.kts       # Project settings
└── README.md                 # Project documentation
```

## Building

### Build Plugin

```bash
./gradlew clean buildPlugin
```

Output: `build/distributions/parquet-studio-1.0.0.zip`

### Run Plugin in Sandbox

```bash
./gradlew runIde
```

This starts IntelliJ IDEA with the plugin installed in a sandbox environment.

### Run Tests

```bash
./gradlew test
```

### Clean Build

```bash
./gradlew clean
```

## Code Structure

### Package Organization

All code is in `com.github.jhordyhuaman.parquetstudio`:

- **Service Layer**: `DuckDBParquetService` - Data operations
- **Model Layer**: `ParquetTableModel`, `ParquetData` - Data structures
- **UI Layer**: `ParquetToolWindow`, `ParquetToolWindowFactory` - User interface

### Key Classes

#### DuckDBParquetService

Handles all DuckDB operations:
- Driver loading (static initializer)
- Connection management
- SQL execution
- Type normalization

#### ParquetTableModel

Swing table model:
- Extends `AbstractTableModel`
- Type validation
- CRUD operations
- Data conversion

#### ParquetToolWindow

Main UI component:
- Toolbar with actions
- JTable for data display
- Status bar
- Event handling

## Debugging

### IDE Logs

View logs in IntelliJ IDEA:
- Help → Show Log in Finder/Explorer
- Look for `idea.log`

### Plugin Logs

Our code uses IntelliJ Logger:
```java
private static final Logger LOGGER = Logger.getInstance(ParquetToolWindow.class);
LOGGER.info("Message");
LOGGER.error("Error", exception);
```

### Debugging Plugin

1. Set breakpoints in your code
2. Run: `./gradlew runIde --debug-jvm`
3. IntelliJ will start in debug mode
4. Breakpoints will be hit

## Testing

### Unit Tests

Place tests in `src/test/java`:
```java
@Test
void testLoadParquet() {
    // Test implementation
}
```

### Manual Testing

1. Run `./gradlew runIde`
2. Open Parquet Studio tool window
3. Test all features manually
4. Check logs for errors

## Common Tasks

### Adding a New Feature

1. Create feature branch
2. Implement in appropriate class
3. Add tests
4. Update documentation
5. Test manually
6. Create PR

### Fixing a Bug

1. Reproduce the bug
2. Identify root cause
3. Fix the issue
4. Add test to prevent regression
5. Test fix
6. Update CHANGELOG

### Refactoring

1. Ensure tests pass
2. Make refactoring changes
3. Run tests again
4. Verify functionality
5. Update documentation if needed

## IntelliJ Platform Concepts

### Tool Windows

Our plugin registers a tool window:
- Defined in `plugin.xml`
- Created by `ParquetToolWindowFactory`
- Implemented by `ParquetToolWindow`

### Actions

Toolbar buttons are standard Swing components:
- `JButton` with `ActionListener`
- Use IntelliJ `Messages` for dialogs

### Services

We use a simple service pattern:
- `DuckDBParquetService` is a singleton
- Created once per tool window instance

## Best Practices

### Error Handling

- Always catch and log exceptions
- Show user-friendly error messages
- Use IntelliJ `Messages` for dialogs

### Logging

- Use IntelliJ Logger, not System.out
- Log at appropriate levels (info, warn, error)
- Include context in log messages

### Threading

- Use `SwingWorker` for long operations
- Keep UI updates on EDT
- Background work in `doInBackground()`

### Type Safety

- Validate types before conversion
- Provide clear error messages
- Handle edge cases

## Troubleshooting

### Build Fails

- Check JDK version (must be 17+)
- Verify Gradle wrapper version
- Clean and rebuild: `./gradlew clean build`

### Plugin Not Loading

- Check `plugin.xml` syntax
- Verify class names are correct
- Check IDE logs for errors

### DuckDB Driver Not Found

- Verify dependency in `build.gradle.kts`
- Check that JAR includes dependencies
- Review driver loading logs

## Resources

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [DuckDB Documentation](https://duckdb.org/docs/)
- [Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)

## Getting Help

- Check existing [Issues](https://github.com/jhordyhuaman/parquet-studio/issues)
- Review [Architecture](ARCHITECTURE.md) docs
- Open a [Discussion](https://github.com/jhordyhuaman/parquet-studio/discussions)

