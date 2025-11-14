/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jhordyhuaman.parquetstudio;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for Parquet data with type validation.
 */
public class ParquetTableModel extends AbstractTableModel {
  private static final Logger LOGGER = Logger.getInstance(ParquetTableModel.class);

  private final List<String> columnNames;
  private final List<String> columnTypes;
  private final List<List<Object>> rows;

  public ParquetTableModel(List<String> columnNames, List<String> columnTypes, List<List<Object>> rows) {
    this.columnNames = new ArrayList<>(columnNames);
    this.columnTypes = new ArrayList<>(columnTypes);
    this.rows = new ArrayList<>();
    for (List<Object> row : rows) {
      this.rows.add(new ArrayList<>(row));
    }
  }

  @Override
  public int getRowCount() {
    return rows.size();
  }

  @Override
  public int getColumnCount() {
    return columnNames.size();
  }

  @Override
  public String getColumnName(int column) {
    if (column >= 0 && column < columnNames.size()) {
        String templateColumnName = "<html><center><strong>%s</strong><br><span style='font-size:10px;color:gray;'>%s</span></center></html>";
        return templateColumnName.formatted(columnNames.get(column), columnTypes.get(column).toLowerCase());
    }
    return "";
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex >= 0 && columnIndex < columnTypes.size()) {
      String type = columnTypes.get(columnIndex);
      if (type.contains("BOOLEAN")) return Boolean.class;
      if (type.contains("INTEGER")) return Integer.class;
      if (type.contains("BIGINT")) return Long.class;
      if (type.contains("DOUBLE")) return Double.class;
      if (type.contains("DATE")) return LocalDate.class;
      if (type.contains("TIMESTAMP")) return LocalDateTime.class;
    }
    return String.class;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex >= 0 && rowIndex < rows.size() && columnIndex >= 0 && columnIndex < columnNames.size()) {
      List<Object> row = rows.get(rowIndex);
      if (columnIndex < row.size()) {
        return row.get(columnIndex);
      }
    }
    return null;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= rows.size() || columnIndex < 0 || columnIndex >= columnNames.size()) {
      return;
    }

    String stringValue = aValue != null ? aValue.toString() : null;
    String columnType = columnTypes.get(columnIndex);

    try {
      Object convertedValue = convertValue(stringValue, columnType);
      List<Object> row = rows.get(rowIndex);
      
      while (row.size() <= columnIndex) {
        row.add(null);
      }
      
      row.set(columnIndex, convertedValue);
      fireTableCellUpdated(rowIndex, columnIndex);
    } catch (Exception e) {
      LOGGER.error("Error setting value: " + e.getMessage(), e);
      Messages.showErrorDialog(
          "Error converting value to " + columnType + ": " + e.getMessage(),
          "Conversion Error");
    }
  }

  private Object convertValue(String stringValue, String columnType) {
    if (stringValue == null || stringValue.trim().isEmpty()) {
      return null;
    }

    String trimmed = stringValue.trim();

    try {
      if (columnType.contains("BOOLEAN")) {
        return parseBoolean(trimmed);
      } else if (columnType.contains("INTEGER")) {
        return Integer.parseInt(trimmed);
      } else if (columnType.contains("BIGINT")) {
        return Long.parseLong(trimmed);
      } else if (columnType.contains("DOUBLE")) {
        return Double.parseDouble(trimmed);
      } else if (columnType.contains("DATE")) {
        return LocalDate.parse(trimmed);
      } else if (columnType.contains("TIMESTAMP")) {
        return LocalDateTime.parse(trimmed);
      } else {
        return trimmed;
      }
    } catch (NumberFormatException | DateTimeParseException e) {
      throw new IllegalArgumentException("Cannot convert '" + trimmed + "' to " + columnType);
    }
  }

  private Boolean parseBoolean(String s) {
    String x = s.toLowerCase(Locale.ROOT);
    return x.equals("true") || x.equals("1") || x.equals("yes") || x.equals("y");
  }

  public void addRow() {
    List<Object> newRow = new ArrayList<>();
    for (int i = 0; i < columnNames.size(); i++) {
      String type = columnTypes.get(i);
      Object defaultValue = getDefaultValue(type);
      newRow.add(defaultValue);
    }
    rows.add(newRow);
    int newRowIndex = rows.size() - 1;
    fireTableRowsInserted(newRowIndex, newRowIndex);
  }

  public void deleteRow(int rowIndex) {
    if (rowIndex >= 0 && rowIndex < rows.size()) {
      rows.remove(rowIndex);
      fireTableRowsDeleted(rowIndex, rowIndex);
    }
  }

  public void deleteRows(int[] rowIndices) {
    if (rowIndices == null || rowIndices.length == 0) {
      return;
    }

    int[] sorted = java.util.Arrays.stream(rowIndices)
        .distinct()
        .sorted()
        .toArray();

    for (int i = sorted.length - 1; i >= 0; i--) {
      deleteRow(sorted[i]);
    }
  }

  private Object getDefaultValue(String type) {
    if (type.contains("BOOLEAN")) return false;
    if (type.contains("INTEGER")) return 0;
    if (type.contains("BIGINT")) return 0L;
    if (type.contains("DOUBLE")) return 0.0;
    if (type.contains("DATE")) return null;
    if (type.contains("TIMESTAMP")) return null;
    return "";
  }

  public ParquetData toParquetData() {
    return new ParquetData(columnNames, columnTypes, rows);
  }
}

