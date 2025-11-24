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
package com.github.jhordyhuaman.parquetstudio.ui;

import com.intellij.openapi.ui.DialogWrapper;
import java.awt.*;
import javax.swing.*;

/**
 * Dialog for adding a new column to a Parquet table.
 */
public class AddColumnDialog extends DialogWrapper {
  private JTextField columnNameField;
  private JComboBox<String> columnTypeComboBox;
  private static final String[] COLUMN_TYPES = {
    "VARCHAR", "INTEGER", "BIGINT", "DOUBLE", "BOOLEAN", "DATE", "TIMESTAMP"
  };

  public AddColumnDialog(Component parent) {
    super(parent, true);
    setTitle("Add Column");
    init();
  }

  @Override
  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    // Column name
    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(new JLabel("Column Name:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    columnNameField = new JTextField(20);
    panel.add(columnNameField, gbc);

    // Column type
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    panel.add(new JLabel("Column Type:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    columnTypeComboBox = new JComboBox<>(COLUMN_TYPES);
    columnTypeComboBox.setSelectedItem("VARCHAR");
    panel.add(columnTypeComboBox, gbc);

    panel.setPreferredSize(new Dimension(400, 100));
    return panel;
  }

  @Override
  protected void doOKAction() {
    String columnName = columnNameField.getText().trim();
    if (columnName.isEmpty()) {
      JOptionPane.showMessageDialog(
          getContentPanel(),
          "Column name cannot be empty.",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    // Validate column name (basic validation - no spaces, special chars)
    if (!columnName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
      JOptionPane.showMessageDialog(
          getContentPanel(),
          "Column name must start with a letter or underscore and contain only letters, numbers, and underscores.",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    super.doOKAction();
  }

  public String getColumnName() {
    return columnNameField.getText().trim();
  }

  public String getColumnType() {
    return (String) columnTypeComboBox.getSelectedItem();
  }
}

