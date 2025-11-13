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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParquetTableModelTest {

  private ParquetTableModel model;
  private List<String> columnNames;
  private List<String> columnTypes;
  private List<List<Object>> rows;

  @BeforeEach
  void setUp() {
    columnNames = new ArrayList<>();
    columnNames.add("id");
    columnNames.add("name");
    columnNames.add("active");

    columnTypes = new ArrayList<>();
    columnTypes.add("INTEGER");
    columnTypes.add("VARCHAR");
    columnTypes.add("BOOLEAN");

    rows = new ArrayList<>();
    List<Object> row1 = new ArrayList<>();
    row1.add(1);
    row1.add("Alice");
    row1.add(true);
    rows.add(row1);

    List<Object> row2 = new ArrayList<>();
    row2.add(2);
    row2.add("Bob");
    row2.add(false);
    rows.add(row2);

    model = new ParquetTableModel(columnNames, columnTypes, rows);
  }

  @Test
  @DisplayName("Should return correct row and column counts")
  void testRowAndColumnCounts() {
    assertThat(model.getRowCount()).isEqualTo(2);
    assertThat(model.getColumnCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("Should return correct column names with types")
  void testColumnNames() {
    assertThat(model.getColumnName(0)).isEqualTo("id (INTEGER)");
    assertThat(model.getColumnName(1)).isEqualTo("name (VARCHAR)");
    assertThat(model.getColumnName(2)).isEqualTo("active (BOOLEAN)");
  }

  @Test
  @DisplayName("Should return correct column classes")
  void testColumnClasses() {
    assertThat(model.getColumnClass(0)).isEqualTo(Integer.class);
    assertThat(model.getColumnClass(1)).isEqualTo(String.class);
    assertThat(model.getColumnClass(2)).isEqualTo(Boolean.class);
  }

  @Test
  @DisplayName("Should return correct cell values")
  void testGetValueAt() {
    assertThat(model.getValueAt(0, 0)).isEqualTo(1);
    assertThat(model.getValueAt(0, 1)).isEqualTo("Alice");
    assertThat(model.getValueAt(0, 2)).isEqualTo(true);
    assertThat(model.getValueAt(1, 0)).isEqualTo(2);
    assertThat(model.getValueAt(1, 1)).isEqualTo("Bob");
    assertThat(model.getValueAt(1, 2)).isEqualTo(false);
  }

  @Test
  @DisplayName("Should indicate all cells are editable")
  void testIsCellEditable() {
    assertThat(model.isCellEditable(0, 0)).isTrue();
    assertThat(model.isCellEditable(0, 1)).isTrue();
    assertThat(model.isCellEditable(1, 2)).isTrue();
  }

  @Test
  @DisplayName("Should update cell values with type conversion")
  void testSetValueAt() {
    // Update INTEGER
    model.setValueAt("42", 0, 0);
    assertThat(model.getValueAt(0, 0)).isEqualTo(42);

    // Update VARCHAR
    model.setValueAt("Charlie", 0, 1);
    assertThat(model.getValueAt(0, 1)).isEqualTo("Charlie");

    // Update BOOLEAN
    model.setValueAt("false", 0, 2);
    assertThat(model.getValueAt(0, 2)).isEqualTo(false);
  }

  @Test
  @DisplayName("Should add a new row with default values")
  void testAddRow() {
    int initialRowCount = model.getRowCount();
    model.addRow();

    assertThat(model.getRowCount()).isEqualTo(initialRowCount + 1);
    assertThat(model.getValueAt(initialRowCount, 0)).isEqualTo(0); // INTEGER default
    assertThat(model.getValueAt(initialRowCount, 1)).isEqualTo(""); // VARCHAR default
    assertThat(model.getValueAt(initialRowCount, 2)).isEqualTo(false); // BOOLEAN default
  }

  @Test
  @DisplayName("Should delete a row")
  void testDeleteRow() {
    int initialRowCount = model.getRowCount();
    model.deleteRow(0);

    assertThat(model.getRowCount()).isEqualTo(initialRowCount - 1);
    assertThat(model.getValueAt(0, 0)).isEqualTo(2); // Second row becomes first
  }

  @Test
  @DisplayName("Should delete multiple rows")
  void testDeleteRows() {
    int initialRowCount = model.getRowCount();
    model.deleteRows(new int[]{0, 1});

    assertThat(model.getRowCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should convert to ParquetData")
  void testToParquetData() {
    ParquetData data = model.toParquetData();

    assertThat(data.getColumnNames()).isEqualTo(columnNames);
    assertThat(data.getColumnTypes()).isEqualTo(columnTypes);
    assertThat(data.getRows()).hasSize(2);
  }

  @Test
  @DisplayName("Should handle null values")
  void testNullValues() {
    model.setValueAt(null, 0, 0);
    assertThat(model.getValueAt(0, 0)).isNull();
  }

  @Test
  @DisplayName("Should handle empty string values")
  void testEmptyStringValues() {
    model.setValueAt("", 0, 1);
    // Empty strings are converted to null in convertValue
    assertThat(model.getValueAt(0, 1)).isNull();
  }
}

