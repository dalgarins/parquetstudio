# Parquet Studio

[![Build](https://github.com/jhordyhuaman/parquetstudio/actions/workflows/build.yml/badge.svg)](https://github.com/jhordyhuaman/parquetstudio/actions/workflows/build.yml)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.github.jhordyhuaman.parquetstudio.svg)](https://plugins.jetbrains.com/plugin/com.github.jhordyhuaman.parquetstudio)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.jhordyhuaman.parquetstudio.svg)](https://plugins.jetbrains.com/plugin/com.github.jhordyhuaman.parquetstudio)

**Professional CRUD editor for Parquet files directly in IntelliJ IDEA**

Parquet Studio is an IntelliJ IDEA plugin that provides a powerful, user-friendly interface for viewing, editing, and managing Parquet files. Built on DuckDB, it offers seamless CRUD operations with type-safe editing and real-time search capabilities.

## ✨ Features

- **📂 Open Parquet Files** - Load and view Parquet files with automatic schema detection
- **✏️ Edit Cells** - Direct cell editing with automatic type validation (INTEGER, DOUBLE, BOOLEAN, VARCHAR, DATE, TIMESTAMP)
- **➕ Add Rows** - Insert new rows with type-appropriate default values
- **🗑️ Delete Rows** - Remove selected rows with confirmation
- **🔍 Search** - Real-time search across all columns with filtering
- **💾 Save As** - Export edited data to new Parquet files using DuckDB
- **📊 Type Safety** - Automatic type conversion and validation
- **⚡ Performance** - Powered by DuckDB for fast read/write operations

## 🚀 Quick Start

### Installation

1. Open IntelliJ IDEA
2. Go to `File` → `Settings` → `Plugins`
3. Search for "Parquet Studio"
4. Click `Install`
5. Restart IntelliJ IDEA

### Usage

1. Open the **Parquet Studio** tool window (View → Tool Windows → Parquet Studio)
2. Click **Open Parquet** to select a `.parquet` file
3. Edit cells directly in the table
4. Use **Add Row** to insert new rows
5. Use **Delete Row** to remove selected rows
6. Use **Search** to filter rows
7. Click **Save As...** to export your changes

## 📖 Documentation

- [Getting Started](docs/GET_STARTED.md) - Detailed setup and usage guide
- [Architecture](docs/ARCHITECTURE.md) - Technical architecture and design decisions
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
- **ParquetToolWindow** - Main UI component with toolbar and table
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
