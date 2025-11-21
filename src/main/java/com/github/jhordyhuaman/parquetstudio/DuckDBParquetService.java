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
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

/**
 * Service for reading and writing Parquet files using DuckDB.
 */
public class DuckDBParquetService implements DataConvertService {
  private static final Logger LOGGER = Logger.getInstance(DuckDBParquetService.class);
  private static final String DUCKDB_JDBC_URL = "jdbc:duckdb:";
  private static boolean driverLoaded = false;

  static {
    try {
      LOGGER.info("Attempting to load DuckDB JDBC driver...");
      Class<?> driverClass = Class.forName("org.duckdb.DuckDBDriver");
      Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
      DriverManager.registerDriver(driver);
      driverLoaded = true;
      LOGGER.info("DuckDB JDBC driver loaded successfully");
    } catch (Exception e) {
      LOGGER.error("Failed to load DuckDB JDBC driver", e);
      driverLoaded = false;
    }
  }

  /**
   * Loads a Parquet file and returns its data.
   */
  public ParquetData loadParquet(File file) throws Exception {
    LOGGER.info("Loading Parquet file: " + file.getAbsolutePath());
    LOGGER.info("Driver loaded status: " + driverLoaded);

    // Log available drivers for debugging
    Enumeration<Driver> drivers = DriverManager.getDrivers();
    LOGGER.info("Available JDBC drivers:");
    while (drivers.hasMoreElements()) {
      Driver d = drivers.nextElement();
      LOGGER.info("  - " + d.getClass().getName());
    }

    if (!driverLoaded) {
      throw new SQLException("DuckDB JDBC driver not loaded. Check classpath for org.duckdb:duckdb_jdbc dependency.");
    }

    LOGGER.info("Attempting to create connection to: " + DUCKDB_JDBC_URL);
    try (Connection conn = DriverManager.getConnection(DUCKDB_JDBC_URL)) {
      LOGGER.info("Connection established successfully");
      List<String> columnNames = new ArrayList<>();
      List<String> columnTypes = new ArrayList<>();

      // Detect schema
      String sql = "SELECT * FROM read_parquet(?) LIMIT 0";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, file.getAbsolutePath());
        try (ResultSet rs = ps.executeQuery()) {
          ResultSetMetaData md = rs.getMetaData();
          int n = md.getColumnCount();
          for (int i = 1; i <= n; i++) {
            columnNames.add(md.getColumnLabel(i));
            String type = md.getColumnTypeName(i).toUpperCase(Locale.ROOT);
            columnTypes.add(normalizeType(type));
          }
        }
      }

      // Load all data
      List<List<Object>> rows = new ArrayList<>();
      String readAll = "SELECT * FROM read_parquet(?)";
      try (PreparedStatement ps = conn.prepareStatement(readAll)) {
        ps.setString(1, file.getAbsolutePath());
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnNames.size(); i++) {
              Object val = rs.getObject(i);
              row.add(val);
            }
            rows.add(row);
          }
        }
      }

      LOGGER.info(
          String.format(
              "Loaded: %d columns, %d rows", columnNames.size(), rows.size()));
      return new ParquetData(columnNames, columnTypes, rows);
    } catch (SQLException e) {
      LOGGER.error("SQL Exception while loading Parquet file", e);
      LOGGER.error("SQL State: " + e.getSQLState());
      LOGGER.error("Error Code: " + e.getErrorCode());
      throw e;
    } catch (Exception e) {
      LOGGER.error("Unexpected exception while loading Parquet file", e);
      throw e;
    }
  }

    public void saveParquet(File file, ParquetData data) throws Exception {
        saveParquet(file, data, null);
    }

  /**
   * Saves ParquetData to a new Parquet file.
   */
  public void saveParquet(File file, ParquetData data, SchemaStructure schema) throws Exception {
    LOGGER.info("Saving Parquet file: " + file.getAbsolutePath());

    if (data.getColumnNames().isEmpty()) {
      throw new IllegalArgumentException("No columns to save");
    }

    if (!driverLoaded) {
      throw new SQLException("DuckDB JDBC driver not loaded. Check classpath for org.duckdb:duckdb_jdbc dependency.");
    }

    if(schema != null) convertTypes(data, schema);

    LOGGER.info("Attempting to create connection to: " + DUCKDB_JDBC_URL);
    try (Connection conn = DriverManager.getConnection(DUCKDB_JDBC_URL)) {
      LOGGER.info("Connection established successfully");
      String tempTable = "temp_table_" + System.currentTimeMillis();

      // Create temporary table
      StringBuilder ddl = new StringBuilder("CREATE TABLE ").append(tempTable).append(" (");
      for (int i = 0; i < data.getColumnNames().size(); i++) {
        if (i > 0) ddl.append(", ");
        String colName = data.getColumnNames().get(i);
        String colType = data.getColumnTypes().get(i);
        ddl.append(escapeIdent(colName)).append(" ").append(colType);
      }
      ddl.append(")");
      try (Statement st = conn.createStatement()) {
        st.execute(ddl.toString());
      }

      // Insert rows
      StringBuilder ins = new StringBuilder("INSERT INTO ").append(tempTable).append(" VALUES (");
      for (int i = 0; i < data.getColumnNames().size(); i++) {
        if (i > 0) ins.append(", ");
        ins.append("?");
      }
      ins.append(")");

      try (PreparedStatement ps = conn.prepareStatement(ins.toString())) {
        for (List<Object> row : data.getRows()) {
          for (int i = 0; i < data.getColumnNames().size(); i++) {
            Object val = row.size() > i ? row.get(i) : null;
            setParameter(ps, i + 1, val);
          }
          ps.addBatch();
        }
        ps.executeBatch();
      }

      // Export to Parquet
      String copy =
          "COPY (SELECT * FROM "
              + tempTable
              + ") TO '"
              + file.getAbsolutePath().replace("'", "''")
              + "' (FORMAT PARQUET)";

      try (Statement st = conn.createStatement()) {
        st.execute(copy);
      }

      LOGGER.info("Parquet file saved: " + file.getAbsolutePath());
    }
  }

  private String normalizeType(String type) {
    if (type.contains("BOOL")) return "BOOLEAN";
    if (type.contains("INT")) {
      if (type.contains("BIG")) return "BIGINT";
      return "INTEGER";
    }
    if (type.contains("DOUBLE") || type.contains("FLOAT")) return "DOUBLE";
    if (type.contains("DATE") && !type.contains("TIME")) return "DATE";
    if (type.contains("TIMESTAMP")) return "TIMESTAMP";
    return "VARCHAR";
  }

  private void setParameter(PreparedStatement ps, int index, Object val) throws SQLException {
    if (val == null) {
      ps.setObject(index, null);
    } else if (val instanceof Boolean) {
      ps.setBoolean(index, (Boolean) val);
    } else if (val instanceof Integer) {
      ps.setInt(index, (Integer) val);
    } else if (val instanceof Long) {
      ps.setLong(index, (Long) val);
    } else if (val instanceof Double) {
      ps.setDouble(index, (Double) val);
    } else if (val instanceof LocalDate) {
      ps.setDate(index, Date.valueOf((LocalDate) val));
    } else if (val instanceof LocalDateTime) {
      ps.setTimestamp(index, Timestamp.valueOf((LocalDateTime) val));
    } else {
      ps.setString(index, val.toString());
    }
  }

  private String escapeIdent(String ident) {
    return '"' + ident.replace("\"", "\"\"") + '"';
  }
}

