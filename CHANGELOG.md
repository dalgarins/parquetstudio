# Changelog

All notable changes to Parquet Studio will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-11-12

### Added
- Initial release of Parquet Studio
- Open Parquet files using DuckDB
- View Parquet data in editable JTable
- Edit cells with automatic type validation
- Add new rows with type-appropriate defaults
- Delete selected rows
- Real-time search across all columns
- Save edited data to new Parquet files
- Support for INTEGER, DOUBLE, BOOLEAN, VARCHAR, DATE, TIMESTAMP types
- Status bar showing row count and file name
- Toolbar with all CRUD operations
- Comprehensive logging for debugging

### Technical
- Built on DuckDB JDBC 0.10.2
- IntelliJ Platform Plugin SDK
- Java 17 compatibility
- Swing-based UI with JBTable

[1.0.0]: https://github.com/jhordyhuaman/parquet-studio/releases/tag/v1.0.0

