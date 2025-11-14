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
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

/**
 * Main tool window panel for Parquet Studio.
 */
public class ParquetToolWindow extends JPanel {
  private static final Logger LOGGER = Logger.getInstance(ParquetToolWindow.class);

  private final DuckDBParquetService service;
  private ParquetTableModel tableModel;
  private JBTable dataTable;
  private JLabel statusLabel;
  private JTextField searchField;
  private JButton searchButton;
  private JButton addRowButton;
  private JButton deleteRowButton;
  private JButton saveAsButton;
  private TableRowSorter<TableModel> rowSorter;
  private File currentFile;

  public ParquetToolWindow() {
    this.service = new DuckDBParquetService();
    initializeUI();
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

    // Open button
    JButton openButton = new JButton("Open Parquet");
    openButton.addActionListener(e -> openParquetFile());
    toolbar.add(openButton);

    toolbar.add(new JSeparator(SwingConstants.VERTICAL));

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
    if (deleteRowButton != null) deleteRowButton.setEnabled(hasData);
    if (saveAsButton != null) saveAsButton.setEnabled(hasData);
    if (searchField != null) searchField.setEnabled(hasData);
  }

  private void openParquetFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Open Parquet File");
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

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      loadParquetFile(selectedFile);
    }
  }

  private void loadParquetFile(File file) {
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
                  Messages.showErrorDialog("Error saving file: " + e.getCause().getMessage(), "Error");
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

