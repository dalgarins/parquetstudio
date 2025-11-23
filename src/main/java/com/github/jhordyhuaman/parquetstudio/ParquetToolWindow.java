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
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.*;

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
  private JPanel containerPanel;
  private JPanel dataPanel;
  private JPanel schemaPanel;
  private JButton goSchemaButton;
  private JButton goDataButton;
  private boolean showingPanelData = true;
  private DataConvertService.SchemaStructure schemaStructureOriginal;
  private DataConvertService.SchemaStructure schemaStructureTransform;
  private JCheckBox schemaCheckBox;
  private JCheckBox strictModeCheckBox;
  private JLabel strictModeJLabel;
  private JTextPane jsonTextPane;
  private TableRowSorter<TableModel> rowSorter;
  private File currentFile;
  private File schemaFile;

  public ParquetToolWindow() {
    this.service = new DuckDBParquetService();
    initializeUI();
  }

  private void initializeUI() {
    setLayout(new BorderLayout());
    containerPanel = new JPanel(new CardLayout());

    // SECTION: Data Panel
    dataPanel = new JPanel(new BorderLayout());
    // Toolbar
    JPanel toolbarPanel = createToolbar();
    dataPanel.add(toolbarPanel, BorderLayout.NORTH);

    // Table
    dataTable = new JBTable();
    dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    dataTable.setFillsViewportHeight(true);
    JScrollPane tableScrollPane = new JScrollPane(dataTable);
    dataPanel.add(tableScrollPane, BorderLayout.CENTER);

    containerPanel.add(dataPanel, Constants.DATA_PANEL);

    // SECTION: Schema Panel
    schemaPanel = new JPanel(new BorderLayout());

    JPanel schemaToolbarPanel = createSchemaToolbar();
    schemaPanel.add(schemaToolbarPanel, BorderLayout.NORTH);

    // Json viewer
    JScrollPane jsonScrollPanel = createJsonViewPanel();
    schemaPanel.add(jsonScrollPanel, BorderLayout.CENTER);
    containerPanel.add(schemaPanel, Constants.SCHEMA_PANEL);

    // SECTION: Status Bar
    statusLabel = new JLabel("Ready. Open a Parquet file to begin.");
    statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(containerPanel, BorderLayout.CENTER);
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

    goSchemaButton = new JButton("View Schema");
    goSchemaButton.addActionListener(e -> changePanel() );

    toolbar.add(saveAsButton);
    toolbar.add(goSchemaButton);

    updateButtonStates(false);

    return toolbar;
  }

  private JPanel createSchemaToolbar() {
    JPanel schemaToolbar = new JPanel();
    schemaToolbar.setLayout(new BoxLayout(schemaToolbar, BoxLayout.X_AXIS));
    schemaToolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Load schema
    JButton loadSchemaButton = new JButton("Load Schema");
    loadSchemaButton.addActionListener(e -> loadSchemaFile());
    schemaToolbar.add(loadSchemaButton);
    schemaToolbar.add(new JSeparator(SwingConstants.VERTICAL));

    JPanel checkBoxPanel = new JPanel();
    checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));

    schemaCheckBox = new JCheckBox("Write with this schema");
    schemaCheckBox.setEnabled(false);

    strictModeCheckBox = new JCheckBox("All columns are in parquet");
    strictModeCheckBox.setEnabled(false);
    strictModeCheckBox.addActionListener(e -> {
        if(strictModeCheckBox.isSelected() && !complyStrictMode()){
            strictModeCheckBox.setSelected(false);
            strictModeJLabel.setText(Constants.Message.SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS_2);
            Messages.showWarningDialog(Constants.Message.SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS, "Schema");
        }
    });

    checkBoxPanel.add(schemaCheckBox);
    checkBoxPanel.add(strictModeCheckBox);

    schemaToolbar.add(checkBoxPanel);
    schemaToolbar.add(new JSeparator(SwingConstants.VERTICAL));

    strictModeJLabel = new JLabel("");
    strictModeJLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
    schemaToolbar.add(strictModeJLabel);

    schemaToolbar.add(new JSeparator(SwingConstants.VERTICAL));

    goDataButton = new JButton("Back Data View");
    goDataButton.addActionListener(e -> changePanel() );
    schemaToolbar.add(goDataButton);

    return schemaToolbar;
  }

  private void changePanel() {
      CardLayout cl = (CardLayout) containerPanel.getLayout();
      if(showingPanelData){
          cl.show(containerPanel, Constants.SCHEMA_PANEL);
      }else{
          cl.show(containerPanel, Constants.DATA_PANEL);
      }
      showingPanelData = !showingPanelData;
  }

  private JScrollPane createJsonViewPanel(){
      jsonTextPane = new JTextPane();
      jsonTextPane.setEditable(false);
      jsonTextPane.setText("SCHEMA OF PARQUET");

      return new JScrollPane(jsonTextPane);
  }

  private void writeOriginalSchemaInPanel(java.util.List<String> columnNames, java.util.List<String> columnTypes) throws Exception{
      DataConvertService.SchemaStructure schemaStructure = service.schemaFromLists(columnNames, columnTypes);
      String schemString = service.convertToJsonString(schemaStructure);
      applyJsonHighlighting(schemString);
      schemaStructureOriginal = schemaStructure;

      LOGGER.info("Write schema of parquet in " + Constants.SCHEMA_PANEL);
  }

  private void writeTransformationSchemaInPanel(File schemaFile) throws Exception {
      if(schemaStructureOriginal == null){
        throw new Exception("First load a file parquet");
      }
      DataConvertService.SchemaStructure schemaStructure = service.schemaFromFile(schemaFile.getAbsolutePath());
      schemaStructure.changesTypesFields();

      schemaStructureTransform = schemaStructureOriginal.toTransform(schemaStructure);
      String schemString = service.convertToJsonString(schemaStructureTransform);
      applyJsonHighlighting(schemString);

      LOGGER.warn("Write other schema in " + Constants.SCHEMA_PANEL);
  }

  private void applyJsonHighlighting(String json) {
      StyledDocument doc = jsonTextPane.getStyledDocument();

      StyleContext sc = StyleContext.getDefaultStyleContext();
      AttributeSet keyColor = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, new Color(230, 162, 60));
      AttributeSet stringColor = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, new Color(40, 170, 60));
      AttributeSet numberColor = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, new Color(190, 60, 190));
      AttributeSet braceColor = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, new Color(60, 120, 200));

      try {
          doc.remove(0, jsonTextPane.getText().length());
          doc.insertString(0, json, null);
      } catch (Exception e) { e.printStackTrace(); }

      applyPattern(json, "\"(.*?)\"\\s*:", keyColor, doc);     // keys
      applyPattern(json, ":\\s*\".*?\"", stringColor, doc);    // strings
      applyPattern(json, ":\\s*(\\d+\\.\\d+|\\d+)", numberColor, doc); // números
      applyPattern(json, "\\b(true|false|null)\\b", numberColor, doc); // boolean / null
      applyPattern(json, "[\\{\\}\\[\\]]", braceColor, doc);   // llaves y corchetes
  }

    private void applyPattern(String text, String regex, AttributeSet style, StyledDocument doc) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, false);
        }
    }

    private boolean isValidSchemaFile(File schemaFile){
      if (schemaFile == null || !schemaFile.exists()) {
          Messages.showErrorDialog("Select a schema file that exists.", "Error Schema");
          return false;
      }
      String[] validFormats = {".schema", ".json"};
      String filePath = schemaFile.getPath();

      boolean isValid = Arrays.stream(validFormats).anyMatch(filePath::endsWith);
      if(!isValid){
          Messages.showErrorDialog("Select a valid format: .schema or .json.", "Error Schema");
          return false;
      }
      return true;
  }

  private void loadSchemaFile() {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Select schema file");
      if (schemaFile != null) {
          fileChooser.setCurrentDirectory(schemaFile.getParentFile());
      }

      fileChooser.setFileFilter(new FileFilter() {
          @Override
          public boolean accept(File f) {
              String fileName = f.getName().toLowerCase();
              return f.isDirectory() || fileName.endsWith(".schema") || fileName.endsWith(".json");
          }

          @Override
          public String getDescription() {
              return "Schema Files (*.schema)";
          }
      });

      int result = fileChooser.showSaveDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fileChooser.getSelectedFile();
          if( !isValidSchemaFile(selectedFile) ) return;

          try {
              schemaFile = selectedFile;
              writeTransformationSchemaInPanel(schemaFile);
              strictModeCheckBox.setEnabled(true);

              if(complyStrictMode()){
                  strictModeCheckBox.setSelected(true);
              }else{
                  strictModeCheckBox.setSelected(false);
                  strictModeJLabel.setText(Constants.Message.SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS_2);
                  Messages.showWarningDialog(Constants.Message.SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS, "Schema");
              }
          } catch (Exception e) {
              Messages.showWarningDialog("Can't read the schema.", "Schema File");
              LOGGER.error(e.getMessage());
          }

          schemaCheckBox.setSelected(true);
          schemaCheckBox.setEnabled(true);
      }
  }

  private boolean complyStrictMode() {
      if(schemaStructureOriginal == null || schemaStructureTransform == null){
          Messages.showErrorDialog("Should load parquet and schema file", "Schema");
          LOGGER.warn("parquet or schema file are not loaded.");
          return false;
      }
      return schemaStructureOriginal.fields.size() != schemaStructureTransform.fields.size();
  }

    private void updateButtonStates(boolean hasData) {
    if (searchButton != null) searchButton.setEnabled(hasData);
    if (addRowButton != null) addRowButton.setEnabled(hasData);
    if (deleteRowButton != null) deleteRowButton.setEnabled(hasData);
    if (saveAsButton != null) saveAsButton.setEnabled(hasData);
    if (goSchemaButton != null) goSchemaButton.setEnabled(hasData);
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
                writeOriginalSchemaInPanel(data.getColumnNames(), data.getColumnTypes());
                resetSchemaComponents();
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

  private void resetSchemaComponents(){
      schemaStructureTransform = null;
      schemaFile = null;

      schemaCheckBox.setSelected(false);
      schemaCheckBox.setEnabled(false);

      strictModeCheckBox.setSelected(false);
      strictModeCheckBox.setEnabled(false);
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
                if(schemaCheckBox.isSelected()){
                    if( !isValidSchemaFile(schemaFile) ) throw new Exception("The schema is not valid.");

                    if(strictModeCheckBox.isSelected()){
                        if(!complyStrictMode()) throw new Exception(Constants.Message.SCHEMA_AND_PARQUET_NOT_SAME_COLUMNS);
                        LOGGER.info("writing parquet with other schema (strict mode)...");
                    }
                    LOGGER.warn("Saving with other schema....");
                    service.saveParquet(outputFile, data, schemaStructureTransform);
                }else{
                    LOGGER.warn("Saving with same schema...");
                    service.saveParquet(outputFile, data);
                }
                  LOGGER.info("The parquet was written.");

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

