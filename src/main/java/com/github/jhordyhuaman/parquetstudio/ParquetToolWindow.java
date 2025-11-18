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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

/**
 * Main tool window panel for Parquet Studio with tab support for multiple files.
 */
public class ParquetToolWindow extends JPanel {
  private static final Logger LOGGER = Logger.getInstance(ParquetToolWindow.class);

  private JTabbedPane tabbedPane;
  private final Map<ParquetEditorPanel, Integer> panelToTabIndex = new HashMap<>();
  private JButton openButton;
  private JButton closeTabButton;

  public ParquetToolWindow() {
    initializeUI();
  }

  private void initializeUI() {
    setLayout(new BorderLayout());

    // Toolbar
    JPanel toolbarPanel = createToolbar();
    add(toolbarPanel, BorderLayout.NORTH);

    // Tabbed pane for multiple files
    tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    
    // Add mouse listener for right-click to close tabs
    tabbedPane.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
          if (tabIndex >= 0) {
            closeTab(tabIndex);
          }
        }
      }
    });
    
    // Add change listener to update close button state when tab changes
    tabbedPane.addChangeListener(e -> updateCloseButtonState());
    
    add(tabbedPane, BorderLayout.CENTER);
  }

  private JPanel createToolbar() {
    JPanel toolbar = new JPanel();
    toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
    toolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Open button
    openButton = new JButton("Open Parquet");
    openButton.addActionListener(e -> openParquetFile());
    toolbar.add(openButton);

    toolbar.add(new JSeparator(SwingConstants.VERTICAL));

    // Close Tab button
    closeTabButton = new JButton("Close Tab");
    closeTabButton.addActionListener(e -> closeCurrentTab());
    closeTabButton.setEnabled(false);
    toolbar.add(closeTabButton);

    return toolbar;
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
      openParquetFileInTab(selectedFile);
    }
  }

  /**
   * Opens a Parquet file in a new tab. If the file is already open, switches to that tab.
   *
   * @param file the Parquet file to open
   */
  private void openParquetFileInTab(File file) {
    // Check if file is already open
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      Component component = tabbedPane.getComponentAt(i);
      if (component instanceof ParquetEditorPanel) {
        ParquetEditorPanel panel = (ParquetEditorPanel) component;
        if (panel.hasFile() && panel.getCurrentFile().equals(file)) {
          // File already open, switch to that tab
          tabbedPane.setSelectedIndex(i);
          LOGGER.info("File already open, switching to existing tab: " + file.getName());
          return;
        }
      }
    }

    // Create new editor panel
    ParquetEditorPanel editorPanel = new ParquetEditorPanel();
    editorPanel.loadParquetFile(file);

    // Add to tabbed pane
    String tabTitle = file.getName();
    int tabIndex = tabbedPane.getTabCount();
    tabbedPane.addTab(tabTitle, null, editorPanel, file.getAbsolutePath());
    tabbedPane.setSelectedIndex(tabIndex);
    
    // Store mapping
    panelToTabIndex.put(editorPanel, tabIndex);

    // Update close button state
    updateCloseButtonState();

    LOGGER.info("Opened file in new tab: " + file.getName());
  }

  /**
   * Closes the currently selected tab.
   */
  private void closeCurrentTab() {
    int selectedIndex = tabbedPane.getSelectedIndex();
    if (selectedIndex >= 0) {
      closeTab(selectedIndex);
    }
  }

  /**
   * Closes a tab at the specified index.
   *
   * @param tabIndex the index of the tab to close
   */
  private void closeTab(int tabIndex) {
    if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) {
      return;
    }

    Component component = tabbedPane.getComponentAt(tabIndex);
    if (component instanceof ParquetEditorPanel) {
      ParquetEditorPanel panel = (ParquetEditorPanel) component;
      
      // Remove from mapping
      panelToTabIndex.remove(panel);
      
      // Remove tab
      tabbedPane.removeTabAt(tabIndex);
      
      // Update mappings for remaining tabs
      updateTabMappings();
      
      // Update close button state
      updateCloseButtonState();
      
      LOGGER.info("Closed tab: " + panel.getDisplayName());
    }
  }

  /**
   * Updates the mapping between panels and tab indices after tab removal.
   */
  private void updateTabMappings() {
    panelToTabIndex.clear();
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      Component component = tabbedPane.getComponentAt(i);
      if (component instanceof ParquetEditorPanel) {
        panelToTabIndex.put((ParquetEditorPanel) component, i);
      }
    }
  }

  /**
   * Updates the close button state based on whether there are tabs open.
   */
  private void updateCloseButtonState() {
    if (closeTabButton != null) {
      closeTabButton.setEnabled(tabbedPane.getTabCount() > 0);
    }
  }

  /**
   * Gets the number of open tabs.
   * Useful for testing.
   *
   * @return the number of tabs
   */
  public int getTabCount() {
    return tabbedPane != null ? tabbedPane.getTabCount() : 0;
  }

  /**
   * Gets the currently selected tab index.
   * Useful for testing.
   *
   * @return the selected tab index, or -1 if no tabs
   */
  public int getSelectedTabIndex() {
    return tabbedPane != null ? tabbedPane.getSelectedIndex() : -1;
  }

  /**
   * Gets the editor panel at the specified tab index.
   * Useful for testing.
   *
   * @param tabIndex the tab index
   * @return the ParquetEditorPanel, or null if invalid index
   */
  public ParquetEditorPanel getEditorPanelAt(int tabIndex) {
    if (tabbedPane == null || tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) {
      return null;
    }
    Component component = tabbedPane.getComponentAt(tabIndex);
    if (component instanceof ParquetEditorPanel) {
      return (ParquetEditorPanel) component;
    }
    return null;
  }

  /**
   * Opens a Parquet file in a tab programmatically.
   * Useful for testing.
   *
   * @param file the file to open
   */
  public void openFileInTab(File file) {
    openParquetFileInTab(file);
  }

}

