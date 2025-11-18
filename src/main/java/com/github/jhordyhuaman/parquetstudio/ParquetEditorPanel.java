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
import com.intellij.ui.table.JBTable;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Panel for editing a single Parquet file.
 * This component can be used in tabs to allow editing multiple Parquet files simultaneously.
 */
public class ParquetEditorPanel extends JPanel {
  private static final Logger LOGGER = Logger.getInstance(ParquetEditorPanel.class);

  private final DuckDBParquetService service;
  private ParquetTableModel tableModel;
  private JBTable dataTable;
  private JLabel statusLabel;
  private JTextField searchField;
  private JButton searchButton;
  private JButton addRowButton;
  private JButton addColumnButton;
  private JButton deleteRowButton;
  private JButton deleteColumnButton;
  private JButton saveAsButton;
  private TableRowSorter<TableModel> rowSorter;
  private File currentFile;

  public ParquetEditorPanel() {
    this.service = new DuckDBParquetService();
    initializeUI();
  }

  /**
   * Gets the currently loaded file.
   *
   * @return the current file, or null if no file is loaded
   */
  public File getCurrentFile() {
    return currentFile;
  }

  /**
   * Checks if a file is currently loaded.
   *
   * @return true if a file is loaded, false otherwise
   */
  public boolean hasFile() {
    return currentFile != null;
  }

  /**
   * Gets the display name for this editor (typically the file name).
   *
   * @return the display name
   */
  public String getDisplayName() {
    if (currentFile != null) {
      return currentFile.getName();
    }
    return "Untitled";
  }

  private void initializeUI() {
    setLayout(new BorderLayout());

    // Toolbar
    JPanel toolbarPanel = createToolbar();
    add(toolbarPanel, BorderLayout.NORTH);

    // Table
    dataTable = new JBTable();
    dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    dataTable.setFillsViewportHeight(true);
    JScrollPane tableScrollPane = new JScrollPane(dataTable);
    add(tableScrollPane, BorderLayout.CENTER);

    // Status bar
    statusLabel = new JLabel("Ready. Open a Parquet file to begin.");
    statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(statusLabel, BorderLayout.SOUTH);
  }

  private JPanel createToolbar() {
    JPanel toolbar = new JPanel();
    toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
    toolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Search
    toolbar.add(new JLabel("Search:"));
    searchField = new JTextField(20);
    searchField.setToolTipText("Search in all columns");
    searchField.addActionListener(e -> performSearch());
    toolbar.add(searchField);

    searchButton = new JButton("Search");
    searchButton.addActionListener(e -> performSearch());
    toolbar.add(searchButton);

    toolbar.add(new JSeparator(SwingConstants.VERTICAL));

    // Add Row
    addRowButton = new JButton("Add Row");
    addRowButton.addActionListener(e -> addRow());
    toolbar.add(addRowButton);

    // Add Column
    addColumnButton = new JButton("Add Column");
    addColumnButton.addActionListener(e -> addColumn());
    toolbar.add(addColumnButton);

    // Delete Column
    deleteColumnButton = new JButton("Delete Column");
    deleteColumnButton.addActionListener(e -> deleteSelectedColumn());
    toolbar.add(deleteColumnButton);

    toolbar.add(new JSeparator(SwingConstants.VERTICAL));

    // Delete Row
    deleteRowButton = new JButton("Delete Row");
    deleteRowButton.addActionListener(e -> deleteSelectedRows());
    toolbar.add(deleteRowButton);

    toolbar.add(new JSeparator(SwingConstants.VERTICAL));

    // Save As
    saveAsButton = new JButton("Save As...");
    saveAsButton.addActionListener(e -> saveAsParquet());
    toolbar.add(saveAsButton);

    updateButtonStates(false);

    return toolbar;
  }

  private void updateButtonStates(boolean hasData) {
    if (searchButton != null) searchButton.setEnabled(hasData);
    if (addRowButton != null) addRowButton.setEnabled(hasData);
    if (addColumnButton != null) addColumnButton.setEnabled(hasData);
    if (deleteColumnButton != null) deleteColumnButton.setEnabled(hasData);
    if (deleteRowButton != null) deleteRowButton.setEnabled(hasData);
    if (saveAsButton != null) saveAsButton.setEnabled(hasData);
    if (searchField != null) searchField.setEnabled(hasData);
  }

  /**
   * Loads a Parquet file into this editor.
   *
   * @param file the Parquet file to load
   */
  public void loadParquetFile(File file) {
    try {
      statusLabel.setText("Loading file...");
      SwingWorker<ParquetData, Void> worker =
          new SwingWorker<ParquetData, Void>() {
            @Override
            protected ParquetData doInBackground() throws Exception {
              return service.loadParquet(file);
            }

            @Override
            protected void done() {
              try {
                ParquetData data = get();
                currentFile = file;

                tableModel = new ParquetTableModel(
                    data.getColumnNames(), data.getColumnTypes(), data.getRows());
                dataTable.setModel(tableModel);

                // Configure cell editor for all columns (especially needed for DATE and TIMESTAMP)
                configureCellEditors();

                rowSorter = new TableRowSorter<>(tableModel);
                dataTable.setRowSorter(rowSorter);

                updateButtonStates(true);
                updateStatusLabel();

                LOGGER.info("Loaded: " + file.getName() + " (" + data.getRows().size() + " rows)");
              } catch (Exception e) {
                LOGGER.error("Error loading Parquet file", e);
                Messages.showErrorDialog(
                    "Error loading Parquet file: " + e.getMessage(), "Error");
                statusLabel.setText("Error loading file.");
              }
            }
          };
      worker.execute();
    } catch (Exception e) {
      LOGGER.error("Error loading Parquet file", e);
      Messages.showErrorDialog("Error loading Parquet file: " + e.getMessage(), "Error");
      statusLabel.setText("Error loading file.");
    }
  }

  private void performSearch() {
    if (rowSorter == null || tableModel == null) {
      return;
    }

    String text = searchField.getText();
    if (text.trim().isEmpty()) {
      rowSorter.setRowFilter(null);
    } else {
      final String searchText = text.toLowerCase();
      rowSorter.setRowFilter(
          new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
              for (int i = 0; i < entry.getValueCount(); i++) {
                Object value = entry.getValue(i);
                if (value != null && value.toString().toLowerCase().contains(searchText)) {
                  return true;
                }
              }
              return false;
            }
          });
    }
    updateStatusLabel();
  }

  private void addRow() {
    if (tableModel == null) {
      Messages.showErrorDialog("No data loaded. Please open a file first.", "Error");
      return;
    }

    try {
      int newRowIndex = tableModel.getRowCount();
      tableModel.addRow();
      
      if (rowSorter != null && dataTable.getRowSorter() != null) {
        int viewIndex = dataTable.convertRowIndexToView(newRowIndex);
        dataTable.setRowSelectionInterval(viewIndex, viewIndex);
        dataTable.scrollRectToVisible(dataTable.getCellRect(viewIndex, 0, true));
      } else {
        dataTable.setRowSelectionInterval(newRowIndex, newRowIndex);
        dataTable.scrollRectToVisible(dataTable.getCellRect(newRowIndex, 0, true));
      }
      
      updateStatusLabel();
    } catch (Exception e) {
      LOGGER.error("Error adding row", e);
      Messages.showErrorDialog("Error adding row: " + e.getMessage(), "Error");
    }
  }

  private void addColumn() {
    if (tableModel == null) {
      Messages.showErrorDialog("No data loaded. Please open a file first.", "Error");
      return;
    }

    try {
      AddColumnDialog dialog = new AddColumnDialog(this);
      if (dialog.showAndGet()) {
        String columnName = dialog.getColumnName();
        String columnType = dialog.getColumnType();
        
        tableModel.addColumn(columnName, columnType);
        
        // Configure cell editor for the new column
        TableCellEditor textEditor = new DefaultCellEditor(new JTextField()) {
          @Override
          public Component getTableCellEditorComponent(JTable table, Object value,
              boolean isSelected, int row, int column) {
            Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            JTextField textField = (JTextField) component;
            
            // Convert value to String for display
            if (value == null) {
              textField.setText("");
            } else if (value instanceof LocalDate) {
              textField.setText(value.toString());
            } else if (value instanceof LocalDateTime) {
              textField.setText(value.toString());
            } else {
              textField.setText(value.toString());
            }
            
            return component;
          }
          
          @Override
          public Object getCellEditorValue() {
            JTextField textField = (JTextField) getComponent();
            return textField.getText();
          }
        };
        int newColumnIndex = tableModel.getColumnCount() - 1;
        if (newColumnIndex >= 0) {
          dataTable.getColumnModel().getColumn(newColumnIndex).setCellEditor(textEditor);
          
          // Scroll to the new column
          dataTable.scrollRectToVisible(
              dataTable.getCellRect(0, newColumnIndex, true));
          // Select the new column header
          dataTable.getColumnModel().getSelectionModel()
              .setSelectionInterval(newColumnIndex, newColumnIndex);
        }
        
        updateStatusLabel();
        LOGGER.info("Added column: " + columnName + " (" + columnType + ")");
      }
    } catch (IllegalArgumentException e) {
      LOGGER.error("Error adding column", e);
      Messages.showErrorDialog("Error adding column: " + e.getMessage(), "Error");
    } catch (Exception e) {
      LOGGER.error("Error adding column", e);
      Messages.showErrorDialog("Error adding column: " + e.getMessage(), "Error");
    }
  }

  private void deleteSelectedColumn() {
    if (tableModel == null) {
      Messages.showErrorDialog("No data loaded. Please open a file first.", "Error");
      return;
    }

    // Get selected column
    int selectedColumn = dataTable.getSelectedColumn();
    if (selectedColumn < 0) {
      Messages.showInfoMessage("Please select a column to delete.", "Info");
      return;
    }

    // Convert view column index to model column index
    int modelColumnIndex = dataTable.convertColumnIndexToModel(selectedColumn);
    
    if (modelColumnIndex < 0 || modelColumnIndex >= tableModel.getColumnCount()) {
      Messages.showErrorDialog("Invalid column selection.", "Error");
      return;
    }

    String columnName = tableModel.getColumnName(modelColumnIndex);
    
    // Check if it's the last column
    if (tableModel.getColumnCount() <= 1) {
      Messages.showErrorDialog("Cannot delete the last column. A table must have at least one column.", "Error");
      return;
    }

    // Confirm deletion
    int confirm = Messages.showYesNoDialog(
        "Are you sure you want to delete column '" + columnName + "'?\n" +
        "This action cannot be undone.",
        "Confirm Column Deletion",
        Messages.getQuestionIcon());

    if (confirm == Messages.YES) {
      try {
        tableModel.deleteColumn(modelColumnIndex);
        
        // Reconfigure cell editors after column deletion
        configureCellEditors();
        
        updateStatusLabel();
        LOGGER.info("Deleted column: " + columnName);
      } catch (IllegalArgumentException e) {
        LOGGER.error("Error deleting column", e);
        Messages.showErrorDialog("Error deleting column: " + e.getMessage(), "Error");
      } catch (Exception e) {
        LOGGER.error("Error deleting column", e);
        Messages.showErrorDialog("Error deleting column: " + e.getMessage(), "Error");
      }
    }
  }

  private void deleteSelectedRows() {
    if (tableModel == null) {
      Messages.showErrorDialog("No data loaded. Please open a file first.", "Error");
      return;
    }

    int[] selectedRows = dataTable.getSelectedRows();
    if (selectedRows.length == 0) {
      Messages.showInfoMessage("Please select at least one row to delete.", "Info");
      return;
    }

    int confirm =
        Messages.showYesNoDialog(
            "Are you sure you want to delete " + selectedRows.length + " row(s)?",
            "Confirm Deletion",
            Messages.getQuestionIcon());

    if (confirm == Messages.YES) {
      try {
        int[] modelIndices = new int[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
          modelIndices[i] = dataTable.convertRowIndexToModel(selectedRows[i]);
        }

        RowSorter<?> currentSorter = dataTable.getRowSorter();
        boolean sorterWasEnabled = currentSorter != null;
        if (sorterWasEnabled) {
          dataTable.setRowSorter(null);
        }

        tableModel.deleteRows(modelIndices);

        if (sorterWasEnabled && rowSorter != null) {
          dataTable.setRowSorter(rowSorter);
        }

        updateStatusLabel();
      } catch (Exception e) {
        if (rowSorter != null && dataTable.getRowSorter() == null) {
          dataTable.setRowSorter(rowSorter);
        }
        LOGGER.error("Error deleting rows", e);
        Messages.showErrorDialog("Error deleting rows: " + e.getMessage(), "Error");
      }
    }
  }

  private void saveAsParquet() {
    if (tableModel == null) {
      Messages.showErrorDialog("No data loaded. Please open a file first.", "Error");
      return;
    }

    try {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Save As Parquet");
      if (currentFile != null) {
        fileChooser.setCurrentDirectory(currentFile.getParentFile());
      }
      fileChooser.setFileFilter(
          new FileFilter() {
            @Override
            public boolean accept(File f) {
              return f.isDirectory() || f.getName().toLowerCase().endsWith(".parquet");
            }

            @Override
            public String getDescription() {
              return "Parquet Files (*.parquet)";
            }
          });

      int result = fileChooser.showSaveDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        File outputFile;
        if (!selectedFile.getName().toLowerCase().endsWith(".parquet")) {
          outputFile = new File(selectedFile.getPath() + ".parquet");
        } else {
          outputFile = selectedFile;
        }

        if (outputFile.exists()) {
          int overwrite =
              Messages.showYesNoDialog(
                  "File already exists. Overwrite?",
                  "Confirm Overwrite",
                  Messages.getQuestionIcon());
          if (overwrite != Messages.YES) {
            return;
          }
        }

        statusLabel.setText("Saving file...");
        SwingWorker<Void, Void> saveWorker =
            new SwingWorker<Void, Void>() {
              @Override
              protected Void doInBackground() throws Exception {
                ParquetData data = tableModel.toParquetData();
                service.saveParquet(outputFile, data);
                return null;
              }

              @Override
              protected void done() {
                try {
                  get();
                  statusLabel.setText("File saved: " + outputFile.getName());
                  Messages.showInfoMessage(
                      "File saved successfully: " + outputFile.getPath(), "Success");
                } catch (Exception e) {
                  LOGGER.error("Error saving Parquet file", e);
                  Messages.showErrorDialog("Error saving file: " + e.getMessage(), "Error");
                  statusLabel.setText("Error saving file.");
                }
              }
            };
        saveWorker.execute();
      }
    } catch (Exception e) {
      LOGGER.error("Error saving Parquet file", e);
      Messages.showErrorDialog("Error saving file: " + e.getMessage(), "Error");
    }
  }

  private void configureCellEditors() {
    // Configure a text field editor for all columns
    // This is especially important for DATE and TIMESTAMP columns
    // which don't have default editors in JTable
    TableCellEditor textEditor = new DefaultCellEditor(new JTextField()) {
      @Override
      public Component getTableCellEditorComponent(JTable table, Object value,
          boolean isSelected, int row, int column) {
        Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        JTextField textField = (JTextField) component;
        
        // Convert value to String for display
        if (value == null) {
          textField.setText("");
        } else if (value instanceof LocalDate) {
          textField.setText(value.toString());
        } else if (value instanceof LocalDateTime) {
          textField.setText(value.toString());
        } else {
          textField.setText(value.toString());
        }
        
        return component;
      }
      
      @Override
      public Object getCellEditorValue() {
        // Get the value from the text field
        JTextField textField = (JTextField) getComponent();
        String text = textField.getText();
        // Return as String - the model will handle conversion
        return text;
      }
    };
    
    // Apply the editor to all columns
    for (int i = 0; i < tableModel.getColumnCount(); i++) {
      dataTable.getColumnModel().getColumn(i).setCellEditor(textEditor);
    }
  }

  private void updateStatusLabel() {
    if (tableModel != null && currentFile != null) {
      int rowCount = tableModel.getRowCount();
      int filteredCount =
          rowSorter != null && rowSorter.getRowFilter() != null
              ? rowSorter.getViewRowCount()
              : rowCount;
      if (filteredCount < rowCount) {
        statusLabel.setText(
            String.format(
                "Rows: %d (filtered: %d) | File: %s",
                rowCount, filteredCount, currentFile.getName()));
      } else {
        statusLabel.setText(
            String.format("Rows: %d | File: %s", rowCount, currentFile.getName()));
      }
    }
  }
}

