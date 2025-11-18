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

import com.github.jhordyhuaman.parquetstudio.model.ParquetData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParquetDataTest {

  @Test
  @DisplayName("Should create ParquetData with columns, types, and rows")
  void testCreateParquetData() {
    // Given
    List<String> columnNames = new ArrayList<>();
    columnNames.add("id");
    columnNames.add("name");

    List<String> columnTypes = new ArrayList<>();
    columnTypes.add("INTEGER");
    columnTypes.add("VARCHAR");

    List<List<Object>> rows = new ArrayList<>();
    List<Object> row1 = new ArrayList<>();
    row1.add(1);
    row1.add("Test");
    rows.add(row1);

    // When
    ParquetData data = new ParquetData(columnNames, columnTypes, rows);

    // Then
    assertThat(data.getColumnNames()).hasSize(2);
    assertThat(data.getColumnNames()).containsExactly("id", "name");
    assertThat(data.getColumnTypes()).hasSize(2);
    assertThat(data.getColumnTypes()).containsExactly("INTEGER", "VARCHAR");
    assertThat(data.getRows()).hasSize(1);
    assertThat(data.getRows().get(0)).containsExactly(1, "Test");
  }

  @Test
  @DisplayName("Should handle empty ParquetData")
  void testEmptyParquetData() {
    // Given
    List<String> columnNames = new ArrayList<>();
    List<String> columnTypes = new ArrayList<>();
    List<List<Object>> rows = new ArrayList<>();

    // When
    ParquetData data = new ParquetData(columnNames, columnTypes, rows);

    // Then
    assertThat(data.getColumnNames()).isEmpty();
    assertThat(data.getColumnTypes()).isEmpty();
    assertThat(data.getRows()).isEmpty();
  }
}

