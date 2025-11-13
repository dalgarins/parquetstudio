# Architecture

This document describes the architecture and design decisions of Parquet Studio.

## Overview

Parquet Studio is built on the IntelliJ Platform Plugin SDK and uses DuckDB for Parquet file operations. The architecture follows a clean separation of concerns with distinct layers for data access, business logic, and presentation.

## Architecture Layers

```
┌─────────────────────────────────────┐
│   Presentation Layer (Swing UI)     │
│   - ParquetToolWindow               │
│   - ParquetTableModel               │
└─────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│   Service Layer                     │
│   - DuckDBParquetService            │
└─────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│   Data Layer                        │
│   - DuckDB JDBC                     │
│   - Parquet Files                   │
└─────────────────────────────────────┘
```

## Components

### ParquetToolWindow

**Location**: `com.github.jhordyhuaman.parquetstudio.ParquetToolWindow`

**Responsibility**: Main UI component that orchestrates user interactions.

**Key Features**:
- Toolbar with action buttons
- JTable for data display
- Status bar for feedback
- Event handling for user actions

**Dependencies**:
- `DuckDBParquetService` for data operations
- `ParquetTableModel` for table data
- IntelliJ Platform UI components

### ParquetTableModel

**Location**: `com.github.jhordyhuaman.parquetstudio.ParquetTableModel`

**Responsibility**: Swing table model with type validation and CRUD operations.

**Key Features**:
- Extends `AbstractTableModel`
- Type-safe cell editing
- Row addition/deletion
- Data conversion and validation

**Type Support**:
- INTEGER, BIGINT → Integer, Long
- DOUBLE → Double
- BOOLEAN → Boolean
- VARCHAR → String
- DATE → LocalDate
- TIMESTAMP → LocalDateTime

### DuckDBParquetService

**Location**: `com.github.jhordyhuaman.parquetstudio.DuckDBParquetService`

**Responsibility**: All DuckDB operations for reading and writing Parquet files.

**Key Methods**:
- `loadParquet(File)` - Loads Parquet file and returns ParquetData
- `saveParquet(File, ParquetData)` - Saves ParquetData to file

**Implementation Details**:
- Uses DuckDB JDBC driver
- Creates in-memory connections
- Uses `read_parquet()` function for reading
- Uses `COPY TO ... FORMAT PARQUET` for writing
- Handles type normalization (DuckDB → Standard types)

### ParquetData

**Location**: `com.github.jhordyhuaman.parquetstudio.ParquetData`

**Responsibility**: Data transfer object for Parquet file contents.

**Structure**:
- `List<String> columnNames` - Column names
- `List<String> columnTypes` - Column types (normalized)
- `List<List<Object>> rows` - Row data

## Data Flow

### Loading a Parquet File

```
User clicks "Open Parquet"
    ↓
ParquetToolWindow.openParquetFile()
    ↓
ParquetToolWindow.loadParquetFile()
    ↓
SwingWorker.doInBackground()
    ↓
DuckDBParquetService.loadParquet()
    ↓
DuckDB: SELECT * FROM read_parquet(?) LIMIT 0  (schema)
DuckDB: SELECT * FROM read_parquet(?)           (data)
    ↓
ParquetData created
    ↓
ParquetTableModel initialized
    ↓
JTable updated
```

### Saving a Parquet File

```
User clicks "Save As..."
    ↓
ParquetToolWindow.saveAsParquet()
    ↓
ParquetTableModel.toParquetData()
    ↓
SwingWorker.doInBackground()
    ↓
DuckDBParquetService.saveParquet()
    ↓
DuckDB: CREATE TABLE temp_table_xxx (...)
DuckDB: INSERT INTO temp_table_xxx VALUES (...)
DuckDB: COPY (SELECT * FROM temp_table_xxx) TO 'file.parquet' (FORMAT PARQUET)
    ↓
File saved
```

## Design Decisions

### Why DuckDB?

- **Performance**: Fast Parquet read/write operations
- **Simplicity**: SQL-based interface, no complex Parquet libraries
- **Type Safety**: Automatic type detection and conversion
- **Reliability**: Battle-tested database engine

### Why Swing?

- **Native Integration**: Seamless integration with IntelliJ Platform
- **JBTable**: Enhanced table component from IntelliJ
- **Performance**: Efficient for large datasets
- **Familiarity**: Standard Java UI framework

### Type Normalization

DuckDB returns various type names (TINYINT, SMALLINT, INTEGER, BIGINT, etc.). We normalize these to:
- BOOLEAN
- INTEGER
- BIGINT
- DOUBLE
- VARCHAR
- DATE
- TIMESTAMP

This simplifies the UI and validation logic.

### In-Memory Operations

All data is loaded into memory for editing. This provides:
- Fast editing operations
- Immediate validation
- Simple state management

Trade-off: Large files (>1GB) may consume significant memory.

## Error Handling

### Driver Loading

The DuckDB driver is loaded in a static initializer with comprehensive logging:
- Logs success/failure
- Lists available drivers for debugging
- Provides clear error messages

### SQL Operations

All SQL operations are wrapped in try-catch blocks:
- Logs SQL exceptions with state and error codes
- Provides user-friendly error messages
- Maintains application stability

### Type Conversion

Type conversion errors are caught and displayed:
- Shows conversion error dialog
- Preserves original value
- Logs error for debugging

## Future Enhancements

Potential improvements:
- Streaming for large files
- Column type editing
- Schema modification
- Multiple file tabs
- Export to other formats (CSV, JSON)
- Undo/redo functionality
- Column sorting and filtering

## Dependencies

### Runtime
- `org.duckdb:duckdb_jdbc:0.10.2` - DuckDB JDBC driver

### Platform
- IntelliJ Platform SDK (provided by plugin framework)
- Java 17

### Test
- JUnit 5
- AssertJ

## Performance Considerations

- **Loading**: O(n) where n is number of rows
- **Editing**: O(1) for single cell updates
- **Search**: O(n*m) where n is rows, m is columns (with filtering)
- **Saving**: O(n) for row insertion, O(1) for COPY operation

## Security

- File operations are sandboxed by IntelliJ Platform
- SQL injection prevention via PreparedStatement
- No network operations
- All operations are local

