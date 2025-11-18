# Parquet Studio

[![Build](https://img.shields.io/github/actions/workflow/status/jhordyhuaman/ParquetStudio/build.yml?branch=main)](https://github.com/jhordyhuaman/ParquetStudio/actions)
[![Version](https://img.shields.io/jetbrains/plugin/v/29009-parquet-studio.svg)](https://plugins.jetbrains.com/plugin/29009-parquet-studio)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/29009-parquet-studio.svg)](https://plugins.jetbrains.com/plugin/29009-parquet-studio)

**Professional CRUD editor for Parquet files directly in IntelliJ IDEA**

Parquet Studio is an IntelliJ IDEA plugin that provides a powerful, user-friendly interface for viewing, editing, and managing Parquet files. Built on DuckDB, it offers seamless CRUD operations with type-safe editing and real-time search capabilities.

## ✨ Features

- **📂 Open Parquet Files** - Load and view Parquet files with automatic schema detection
- **📑 Multiple Tabs** - Edit multiple Parquet files simultaneously with tab-based navigation
- **✏️ Edit Cells** - Direct cell editing with automatic type validation (INTEGER, DOUBLE, BOOLEAN, VARCHAR, DATE, TIMESTAMP)
- **➕ Add Rows** - Insert new rows with type-appropriate default values
- **🗑️ Delete Rows** - Remove selected rows with confirmation
- **➕ Add Columns** - Add new columns to your Parquet table with custom name and type
- **🗑️ Delete Columns** - Remove columns from your Parquet table with confirmation
- **🔍 Search** - Real-time search across all columns with filtering
- **💾 Save As** - Export edited data to new Parquet files using DuckDB
- **📊 Type Safety** - Automatic type conversion and validation
- **📅 Flexible Date/Time Parsing** - Supports multiple TIMESTAMP formats (ISO, space-separated, with milliseconds)
- **⚡ Performance** - Powered by DuckDB for fast read/write operations

## 🚀 Quick Start

### Installation

1. Open IntelliJ IDEA
2. Go to `File` → `Settings` → `Plugins`
3. Search for "Parquet Studio"
4. Click `Install`
5. Restart IntelliJ IDEA

### Usage

1. **Automatic File Opening**: Double-click any `.parquet` file in your project - it will automatically open in Parquet Studio!
2. **Manual Opening**: Open the **Parquet Studio** tool window (View → Tool Windows → Parquet Studio) and click the **Open** icon to select a `.parquet` file
3. **Multiple Files**: Each file opens in its own tab for simultaneous editing
4. **Navigation**: Switch between files by clicking on tabs
5. **Close Tabs**: Right-click on a tab or use the **Close** icon button
6. **Edit Cells**: Double-click any cell to edit directly in the table
7. **Add Row**: Click the **Add** icon to insert new rows
8. **Delete Row**: Select rows and click the **Remove** icon to delete them
9. **Add Column**: Click the **Add Column** icon to add new columns with custom name and type
10. **Delete Column**: Select a column header and click the **Remove** icon to delete it
11. **Search**: Use the search field and click the **Search** icon to filter rows (works independently per tab)
12. **Save**: Click the **Save** icon to export your changes to a new Parquet file

## 📖 Documentation

- [Getting Started](docs/GET_STARTED.md) - Detailed setup and usage guide
- [Architecture](docs/ARCHITECTURE.md) - Current technical architecture and design decisions
- [Icons Guide](docs/ICONS_GUIDE.md) - Quick guide for downloading and using icons
- [Contributing](docs/CONTRIBUTING.md) - Guidelines for contributing to the project
- [Releasing](docs/RELEASING.md) - Release process and versioning
- [Plugin Development Guide](docs/PLUGIN_DEV_GUIDE.md) - Development setup and best practices

## 🛠️ Development

### Prerequisites

- JDK 17 or higher
- IntelliJ IDEA 2023.3 or higher
- Gradle 7.6.3

### Build

```bash
./gradlew clean buildPlugin
```

### Run Plugin

```bash
./gradlew runIde
```

### Test

```bash
./gradlew test
```

## 🏗️ Architecture

Parquet Studio uses a clean, modular architecture:

- **DuckDBParquetService** - Handles all DuckDB operations (read/write Parquet)
- **ParquetTableModel** - Swing table model with type validation
- **ParquetToolWindow** - Main UI component with tabbed interface for multiple files
- **ParquetEditorPanel** - Individual editor panel for each Parquet file (one per tab)
- **ParquetData** - Data transfer object for Parquet contents

See [ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed information.

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](docs/CONTRIBUTING.md) for guidelines.

## 📝 License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](LICENSE.txt) for details.

## 🙏 Acknowledgments

- Built with [DuckDB](https://duckdb.org/) for Parquet operations
- Powered by [IntelliJ Platform](https://plugins.jetbrains.com/docs/intellij/welcome.html)

## 📧 Contact

- **Author**: Jhordy Huaman
- **GitHub**: [@jhordyhuaman](https://github.com/jhordyhuaman)
- **Issues**: [GitHub Issues](https://github.com/jhordyhuaman/parquetstudio/issues)

---

Made with ❤️ for the data engineering community
