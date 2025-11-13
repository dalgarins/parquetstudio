# Contributing to Parquet Studio

Thank you for your interest in contributing to Parquet Studio! This document provides guidelines and instructions for contributing.

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Respect different viewpoints and experiences

## How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in [Issues](https://github.com/jhordyhuaman/parquetstudio/issues)
2. If not, create a new issue using the [bug report template](.github/ISSUE_TEMPLATE/bug_report.md)
3. Include:
   - Steps to reproduce
   - Expected behavior
   - Actual behavior
   - IDE version and OS
   - Relevant logs

### Suggesting Features

1. Check existing feature requests
2. Create a new issue using the [feature request template](.github/ISSUE_TEMPLATE/feature_request.md)
3. Describe:
   - The feature and its use case
   - Why it would be valuable
   - Potential implementation approach

### Contributing Code

1. **Fork the repository**
2. **Create a branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
   - Follow the coding style (see below)
   - Add tests if applicable
   - Update documentation
4. **Test your changes**:
   ```bash
   ./gradlew clean buildPlugin
   ./gradlew runIde
   ```
5. **Commit your changes**:
   ```bash
   git commit -m "Add: description of your feature"
   ```
6. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```
7. **Create a Pull Request**

## Development Setup

### Prerequisites

- JDK 17 or higher
- IntelliJ IDEA 2023.3 or higher
- Gradle 7.6.3

### Setup Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/jhordyhuaman/parquetstudio.git
   cd parquetstudio
   ```

2. Open in IntelliJ IDEA:
   - File → Open → Select project directory
   - IntelliJ will import the Gradle project

3. Build the project:
   ```bash
   ./gradlew clean build
   ```

4. Run the plugin:
   ```bash
   ./gradlew runIde
   ```

## Coding Standards

### Java Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods focused and small
- Use `final` for immutable variables

### Code Organization

- One class per file
- Package structure: `com.github.jhordyhuaman.parquetstudio.*`
- Keep related classes together
- Separate concerns (UI, service, data)

### Example

```java
/**
 * Loads a Parquet file and returns its data.
 *
 * @param file the Parquet file to load
 * @return ParquetData containing columns, types, and rows
 * @throws Exception if the file cannot be loaded
 */
public ParquetData loadParquet(File file) throws Exception {
    // Implementation
}
```

## Testing

### Running Tests

```bash
./gradlew test
```

### Writing Tests

- Place tests in `src/test/java`
- Use JUnit 5
- Test both success and error cases
- Mock external dependencies when appropriate

### Example Test

```java
@Test
@DisplayName("Should load Parquet file successfully")
void testLoadParquet() {
    // Given
    File testFile = new File("test.parquet");
    
    // When
    ParquetData data = service.loadParquet(testFile);
    
    // Then
    assertThat(data.getColumnNames()).isNotEmpty();
}
```

## Documentation

### Code Documentation

- Add JavaDoc for all public classes and methods
- Explain complex logic with inline comments
- Keep comments up-to-date with code changes

### User Documentation

- Update `README.md` for user-facing changes
- Update `docs/GET_STARTED.md` for new features
- Add examples when appropriate

## Pull Request Process

1. **Ensure your code builds**:
   ```bash
   ./gradlew clean build
   ```

2. **Run tests**:
   ```bash
   ./gradlew test
   ```

3. **Check code style**:
   - Follow existing code patterns
   - Ensure no warnings

4. **Update documentation**:
   - README.md if needed
   - CHANGELOG.md for user-visible changes
   - Code comments for complex logic

5. **Create PR**:
   - Clear title and description
   - Reference related issues
   - Include screenshots for UI changes

## Review Process

- Maintainers will review your PR
- Address feedback promptly
- Be open to suggestions
- Keep PRs focused and small when possible

## Release Process

See [RELEASING.md](RELEASING.md) for details on the release process.

## Questions?

- Open a [Discussion](https://github.com/jhordyhuaman/parquetstudio/discussions)
- Check existing [Issues](https://github.com/jhordyhuaman/parquetstudio/issues)
- Review [Plugin Development Guide](PLUGIN_DEV_GUIDE.md)

Thank you for contributing! 🎉

