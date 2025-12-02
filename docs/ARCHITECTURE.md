# Architecture

This document describes the current architecture and design decisions of Parquet Studio.

## Overview

Parquet Studio is built on the IntelliJ Platform Plugin SDK and uses DuckDB for Parquet file operations. The architecture follows a **simple 3-layer separation**: UI, Service, and Model layers.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                      UI LAYER                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   Panels     │  │   Dialogs    │  │  Components  │       │
│  │  (Swing UI)  │  │  (Dialogs)   │  │  (Reusable)  │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│         │                  │                  │             │
│         └──────────────────┼──────────────────┘            │
│                            │                                │
└────────────────────────────┼────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         ParquetEditorService                        │   │
│  │  - Carga/guarda archivos                            │   │
│  │  - Operaciones CRUD                                 │   │
│  │  - Validaciones                                     │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │                                 │
└────────────────────────────┼─────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      MODEL LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ ParquetData  │  │ParquetTable  │  │  DuckDB      │     │
│  │  (DTO)       │  │  Model       │  │  Service     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

## Package Structure

```
com.github.jhordyhuaman.parquetstudio/
├── model/                          # Modelos de datos
│   ├── ParquetData.java            # DTO para datos
│   ├── ParquetTableModel.java      # Modelo de tabla Swing
│   ├── SchemaStructure.java        # DTO para esquemas (parquet & externos)
│   └── SchemaItem*.java            # Representación de campos y transformaciones
│
├── service/                        # Servicios (lógica de negocio)
│   ├── ParquetEditorService.java   # Servicio principal del editor
│   │                              # - Carga/guarda archivos
│   │                              # - Operaciones CRUD
│   │                              # - Validaciones
│   ├── DataSchemaService.java      # Servicio de lectura/transformación de esquemas
│   └── DuckDBParquetService.java  # Servicio DuckDB (datos)
│
├── ui/                            # Componentes de UI
│   ├── ParquetEditorPanel.java    # Panel del editor (solo UI)
│   ├── ParquetToolWindow.java    # Ventana principal (solo UI)
│   └── AddColumnDialog.java       # Diálogo agregar columna
│
├── filetype/                      # Gestión de tipos de archivo
│   ├── ParquetFileType.java
│   ├── ParquetFileTypeFactory.java
│   └── ParquetLanguage.java
│
└── factory/                        # Factories
    ├── ParquetToolWindowFactory.java
    ├── ParquetEditorProvider.java
    └── ParquetFileEditor.java
```

## Components

### UI Layer

#### ParquetToolWindow
**Location**: `com.github.jhordyhuaman.parquetstudio.ui.ParquetToolWindow`

**Responsibility**: Main container managing multiple editor tabs.

**Key Features**:
- `JTabbedPane` for multiple file editing
- File opening with duplicate prevention
- Tab management (open, close, switch)

#### ParquetEditorPanel
**Location**: `com.github.jhordyhuaman.parquetstudio.ui.ParquetEditorPanel`

**Responsibility**: UI component for editing a single Parquet file.

**Key Features**:
- `JTable` for data display
- Toolbar with action buttons (search, add row, add column, delete, save)
- Status bar for feedback
- Schema panel to preview the detected schema, load external schemas (`.schema`/`.json`), and toggle strict save mode
- Cell editors for DATE/TIMESTAMP types

**Dependencies**:
- `ParquetEditorService` for business logic
- `ParquetTableModel` for table data

### Service Layer

#### ParquetEditorService
**Location**: `com.github.jhordyhuaman.parquetstudio.service.ParquetEditorService`

**Responsibility**: Business logic for Parquet editor operations.

**Key Methods**:
- `loadParquetFile(File)` - Loads a Parquet file
- `addRow()` - Adds a new row
- `addColumn(String, String)` - Adds a new column
- `deleteColumn(int)` - Deletes a column
- `deleteRows(int[])` - Deletes rows
- `saveParquetFile(File)` - Saves to Parquet file

**Features**:
- Validates data before operations
- Manages table model state
- Coordinates with DuckDBParquetService
- Delegates schema detection/transform to DataSchemaService

#### DuckDBParquetService
**Location**: `com.github.jhordyhuaman.parquetstudio.service.DuckDBParquetService`

**Responsibility**: DuckDB operations for reading and writing Parquet files.

**Key Methods**:
- `loadParquet(File)` - Loads Parquet file and returns ParquetData
- `saveParquet(File, ParquetData)` - Saves ParquetData to file

**Implementation Details**:
- Uses DuckDB JDBC driver
- Creates in-memory connections
- Uses `read_parquet()` function for reading
- Uses `COPY TO ... FORMAT PARQUET` for writing
- Handles type normalization (DuckDB → Standard types)

#### DataSchemaService
**Location**: `com.github.jhordyhuaman.parquetstudio.service.DataSchemaService`

**Responsibility**: Schema generation, comparison, and transformation when saving with an external schema.

**Key Methods**:
- `generateOriginalSchemaString(List<String>, List<String>)` - Builds JSON of the detected Parquet schema and stores it as the baseline.
- `generateTransformSchemaString()` - Loads a user-provided schema file, aligns it with the current columns, and produces a JSON view of source→target types.
- `applyConvertTypes(ParquetData, SchemaStructure)` - Applies the target types to the data before delegating to DuckDB for writing.
- `isSameNumberOfColumns()` - Validates column parity for strict mode.

### Model Layer

#### ParquetData
**Location**: `com.github.jhordyhuaman.parquetstudio.model.ParquetData`

**Responsibility**: Data transfer object for Parquet file contents.

**Structure**:
- `List<String> columnNames` - Column names
- `List<String> columnTypes` - Column types (normalized)
- `List<List<Object>> rows` - Row data

#### ParquetTableModel
**Location**: `com.github.jhordyhuaman.parquetstudio.model.ParquetTableModel`

**Responsibility**: Swing table model with type validation and CRUD operations.

**Key Features**:
- Extends `AbstractTableModel`
- Type-safe cell editing
- Row/column addition/deletion
- Data conversion and validation

**Type Support**:
- INTEGER, BIGINT → Integer, Long
- DOUBLE → Double
- BOOLEAN → Boolean
- VARCHAR → String
- DATE → LocalDate
- TIMESTAMP → LocalDateTime

#### SchemaStructure / SchemaItem*
**Location**: `com.github.jhordyhuaman.parquetstudio.model`

**Responsibility**: Hold schema metadata for both detected and external schemas, including per-field type mappings when rewriting.

**Notes**:
- Supports normalizing types coming from Avro/Parquet (`timestamp_millis` → `timestamp`, `int32` → `integer`, `int64` → `bigint`).
- `SchemaItemTransformSerializer` handles JSON representation for UI display.

## Data Flow

### Loading a Parquet File

```
User clicks "Open Parquet"
    ↓
ParquetToolWindow.openParquetFile()
    ↓
ParquetEditorPanel.loadParquetFile()
    ↓
SwingWorker.doInBackground()
    ↓
ParquetEditorService.loadParquetFile()
    ↓
DuckDBParquetService.loadParquet()
    ↓
DuckDB: SELECT * FROM read_parquet(?) LIMIT 0  (schema)
DuckDB: SELECT * FROM read_parquet(?)           (data)
    ↓
ParquetData created
    ↓
ParquetEditorService.initializeTableModel()
    ↓
ParquetTableModel initialized
    ↓
JTable updated
```

### Adding a Row

```
User clicks "Add Row"
    ↓
ParquetEditorPanel.addRow()
    ↓
ParquetEditorService.addRow()
    ↓
ParquetTableModel.addRow()
    ↓
UI updated
```

### Saving a Parquet File

```
User clicks "Save As..."
    ↓
ParquetEditorPanel.saveAsParquet()
    ↓
SwingWorker.doInBackground()
    ↓
ParquetEditorService.saveParquetFile()
    ↓
ParquetTableModel.toParquetData()
    ↓
DuckDBParquetService.saveParquet()
    ↓
DuckDB: CREATE TABLE temp_table_xxx (...)
DuckDB: INSERT INTO temp_table_xxx VALUES (...)
DuckDB: COPY (SELECT * FROM temp_table_xxx) TO 'file.parquet' (FORMAT PARQUET)
    ↓
File saved
```

### Saving with External Schema

```
User clicks "View Schema" → "Load Schema"
    ↓
ParquetEditorPanel.writeTransformationSchemaInPanel()
    ↓
DataSchemaService.generateTransformSchemaString()
    ↓
SchemaStructure.schemaFromFile() → toTransform(originalSchema)
    ↓
User enables "Write with this schema" (+ strict mode if desired)
    ↓
ParquetEditorPanel.saveAsParquet()
    ↓
ParquetEditorService.saveParquetFile(outputFile, schemaStructureTransform)
    ↓
DataSchemaService.applyConvertTypes() → DuckDBParquetService.saveParquet()
    ↓
File saved with target types
```

## Design Decisions

### Why 3-Layer Architecture?

- **Simplicity**: Easy to understand and maintain
- **Separation**: Clear boundaries between UI and business logic
- **Testability**: Business logic can be tested without Swing
- **Collaboration**: Different developers can work on UI and services independently

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

### Service Layer Validation

`ParquetEditorService` validates operations before execution:
- Checks if data is loaded
- Validates column indices
- Prevents invalid operations (e.g., deleting last column)

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
- Export to other formats (CSV, JSON)
- Undo/redo functionality
- Column sorting and filtering
- Search improvements

## Dependencies

### Runtime
- `org.duckdb:duckdb_jdbc:0.10.2` - DuckDB JDBC driver
- `com.google.code.gson:gson:2.10.1` - JSON parsing/serialization for schemas

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
