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
package com.github.jhordyhuaman.parquetstudio.model;

import java.util.List;

/**
 * Data structure representing Parquet file contents.
 */
public class ParquetData {
  private final List<String> columnNames;
  private final List<String> columnTypes;
  private final List<List<Object>> rows;

  public ParquetData(List<String> columnNames, List<String> columnTypes, List<List<Object>> rows) {
    this.columnNames = columnNames;
    this.columnTypes = columnTypes;
    this.rows = rows;
  }

  public List<String> getColumnNames() {
    return columnNames;
  }

  public List<String> getColumnTypes() {
    return columnTypes;
  }

  public List<List<Object>> getRows() {
    return rows;
  }
}

