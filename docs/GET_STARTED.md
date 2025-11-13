# Getting Started with Parquet Studio

This guide will help you get started with Parquet Studio, from installation to advanced usage.

## Installation

### From IntelliJ Marketplace

1. Open IntelliJ IDEA
2. Navigate to `File` → `Settings` → `Plugins` (or `Preferences` → `Plugins` on macOS)
3. Click the `Marketplace` tab
4. Search for "Parquet Studio"
5. Click `Install`
6. Restart IntelliJ IDEA when prompted

### From Disk

1. Download the plugin ZIP from [Releases](https://github.com/jhordyhuaman/parquetstudio/releases)
2. Open IntelliJ IDEA
3. Navigate to `File` → `Settings` → `Plugins`
4. Click the gear icon → `Install Plugin from Disk...`
5. Select the downloaded ZIP file
6. Restart IntelliJ IDEA

## First Steps

### Opening the Tool Window

1. After installation, go to `View` → `Tool Windows` → `Parquet Studio`
2. The tool window will appear on the right side of the IDE

### Opening Your First Parquet File

1. Click the **Open Parquet** button in the toolbar
2. Navigate to your `.parquet` file
3. Select and open it
4. The file will load and display in the table

## Basic Operations

### Viewing Data

- The table displays all rows and columns from your Parquet file
- Column headers show the column name and type (e.g., `id (INTEGER)`)
- Scroll horizontally and vertically to view all data

### Editing Cells

1. Double-click any cell to edit it
2. Type the new value
3. Press `Enter` to save
4. The value is automatically validated against the column type
5. If validation fails, an error message is displayed

### Adding Rows

1. Click the **Add Row** button
2. A new row is added with default values:
   - `INTEGER`/`BIGINT`: `0`
   - `DOUBLE`: `0.0`
   - `BOOLEAN`: `false`
   - `VARCHAR`: `""`
   - `DATE`/`TIMESTAMP`: `null`
3. Edit the cells to set your desired values

### Deleting Rows

1. Select one or more rows (click and drag, or Ctrl/Cmd+Click)
2. Click the **Delete Row** button
3. Confirm the deletion
4. The selected rows are removed

### Searching

1. Type your search term in the **Search** field
2. Press `Enter` or click the **Search** button
3. The table filters to show only matching rows
4. Clear the search field to show all rows again

### Saving Changes

1. Click **Save As...**
2. Choose a location and filename
3. Click `Save`
4. Your edited data is exported as a new Parquet file

## Supported Data Types

Parquet Studio supports the following DuckDB types:

- **BOOLEAN** - `true`/`false`, `1`/`0`, `yes`/`no`
- **INTEGER** - Whole numbers (e.g., `42`, `-10`)
- **BIGINT** - Large whole numbers (e.g., `1234567890`)
- **DOUBLE** - Decimal numbers (e.g., `3.14`, `-0.5`)
- **VARCHAR** - Text strings
- **DATE** - Dates in ISO format (e.g., `2024-11-12`)
- **TIMESTAMP** - Date and time in ISO format (e.g., `2024-11-12T10:30:00`)

## Tips and Best Practices

### Performance

- Large files (>100MB) may take a moment to load
- Use search to filter large datasets
- Save frequently to avoid data loss

### Data Validation

- Always verify data types before editing
- Invalid values will show an error message
- Date and timestamp values must be in ISO format

### Workflow

1. Open your Parquet file
2. Review the data structure
3. Make your edits
4. Use search to verify changes
5. Save to a new file (preserve original)

## Troubleshooting

### "No suitable driver found for jdbc:duckdb:"

This error indicates the DuckDB driver is not loaded. Ensure:
- The plugin is properly installed
- IntelliJ IDEA has been restarted after installation
- Check the IDE logs for more details

### "Error loading Parquet file"

- Verify the file is a valid Parquet file
- Check file permissions
- Ensure the file is not corrupted
- Review IDE logs for specific error messages

### Cells not editable

- Ensure a file is loaded
- Check that the table model is properly initialized
- Try closing and reopening the tool window

## Next Steps

- Read the [Architecture](ARCHITECTURE.md) documentation
- Check out [Contributing](CONTRIBUTING.md) if you want to help
- Review [Plugin Development Guide](PLUGIN_DEV_GUIDE.md) for development

## Support

If you encounter issues:
1. Check the [GitHub Issues](https://github.com/jhordyhuaman/parquetstudio/issues)
2. Create a new issue with details
3. Include IDE logs if possible

