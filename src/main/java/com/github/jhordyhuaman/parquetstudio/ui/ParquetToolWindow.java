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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import javax.swing.Box;
import javax.swing.filechooser.FileFilter;

/**
 * Main tool window panel for Parquet Studio with tab support for multiple files.
 */
public class ParquetToolWindow extends JPanel {
  private static final Logger LOGGER = Logger.getInstance(ParquetToolWindow.class);
  
  // Track files currently being opened to prevent duplicates
  private static final Set<String> openingFiles = ConcurrentHashMap.newKeySet();

  private JTabbedPane tabbedPane;
  private final Map<ParquetEditorPanel, Integer> panelToTabIndex = new HashMap<>();
  private JButton openButton;

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
    
    // Add mouse listener for right-click to close tabs and click on close area
    tabbedPane.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
          if (tabIndex >= 0) {
            closeTab(tabIndex);
          }
        } else if (SwingUtilities.isLeftMouseButton(e)) {
          // Check if click is in the close area (right side of tab)
          int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
          if (tabIndex >= 0) {
            java.awt.Rectangle tabBounds = tabbedPane.getBoundsAt(tabIndex);
            // Close area is the right 20 pixels of the tab
            int closeAreaStart = tabBounds.x + tabBounds.width - 20;
            if (e.getX() >= closeAreaStart && e.getX() <= tabBounds.x + tabBounds.width) {
              closeTab(tabIndex);
            }
          }
        }
      }
    });
    
    
    add(tabbedPane, BorderLayout.CENTER);
  }

  private JPanel createToolbar() {
    JPanel toolbar = new JPanel();
    toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
    toolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Open button with icon
    openButton = new JButton(IconLoader.getIcon("/icons/ui/sqlFolder/sqlFolder.svg", ParquetEditorPanel.class));
    openButton.setToolTipText("Open Parquet File");
    openButton.addActionListener(e -> openParquetFile());
    toolbar.add(openButton);

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
   * Gets the normalized (canonical) path of a file, falling back to absolute path if needed.
   *
   * @param file the file to get the path for
   * @return the normalized path
   */
  private String getNormalizedPath(File file) {
    try {
      return file.getCanonicalPath();
    } catch (IOException e) {
      return file.getAbsolutePath();
    }
  }

  /**
   * Opens a Parquet file in a new tab. If the file is already open, switches to that tab.
   *
   * @param file the Parquet file to open
   */
  private void openParquetFileInTab(File file) {
    // Get normalized file path for tracking (must be final for lambda)
    final String filePath = getNormalizedPath(file);
    
    // Synchronize to prevent concurrent openings
    synchronized (ParquetToolWindow.class) {
      // Check if file is currently being opened
      if (openingFiles.contains(filePath)) {
        LOGGER.info("File is already being opened, skipping: " + file.getName());
        return;
      }
      
      // Check if file is already open in a tab
      for (int i = 0; i < tabbedPane.getTabCount(); i++) {
        Component component = tabbedPane.getComponentAt(i);
        if (component instanceof ParquetEditorPanel) {
          ParquetEditorPanel panel = (ParquetEditorPanel) component;
          if (panel.hasFile()) {
            File currentFile = panel.getCurrentFile();
            if (currentFile != null) {
              try {
                String currentFilePath = currentFile.getCanonicalPath();
                if (currentFilePath.equals(filePath)) {
                  // File already open, switch to that tab
                  tabbedPane.setSelectedIndex(i);
                  LOGGER.info("File already open, switching to existing tab: " + file.getName());
                  return;
                }
              } catch (IOException e) {
                // If canonical path fails, fall back to equals
                if (currentFile.equals(file)) {
                  tabbedPane.setSelectedIndex(i);
                  LOGGER.info("File already open, switching to existing tab: " + file.getName());
                  return;
                }
              }
            }
          }
        }
      }
      
      // Mark file as being opened
      openingFiles.add(filePath);
    }
    
    try {
      // Create new editor panel
      ParquetEditorPanel editorPanel = new ParquetEditorPanel();
      editorPanel.loadParquetFile(file);

      // Add to tabbed pane (must be on EDT)
      SwingUtilities.invokeLater(() -> {
        synchronized (ParquetToolWindow.class) {
          // Double-check after loading (file might have been opened by another thread)
          for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component component = tabbedPane.getComponentAt(i);
            if (component instanceof ParquetEditorPanel) {
              ParquetEditorPanel panel = (ParquetEditorPanel) component;
              if (panel.hasFile()) {
                File currentFile = panel.getCurrentFile();
                if (currentFile != null) {
                  try {
                    String currentFilePath = currentFile.getCanonicalPath();
                    if (currentFilePath.equals(filePath)) {
                      // File was opened by another thread, just switch to it
                      tabbedPane.setSelectedIndex(i);
                      openingFiles.remove(filePath);
                      LOGGER.info("File opened by another thread, switching to existing tab: " + file.getName());
                      return;
                    }
                  } catch (IOException e) {
                    if (currentFile.equals(file)) {
                      tabbedPane.setSelectedIndex(i);
                      openingFiles.remove(filePath);
                      LOGGER.info("File opened by another thread, switching to existing tab: " + file.getName());
                      return;
                    }
                  }
                }
              }
            }
          }
          
          // Add to tabbed pane
          String tabTitle = file.getName();
          int tabIndex = tabbedPane.getTabCount();
          // Add tab with title that includes close indicator
          // Format: "filename [×]" where × is the close indicator
          String tabTitleWithClose = tabTitle + "  ×";
          tabbedPane.addTab(tabTitleWithClose, null, editorPanel, file.getAbsolutePath());
          tabbedPane.setSelectedIndex(tabIndex);
          
          // Store mapping
          panelToTabIndex.put(editorPanel, tabIndex);
          
          // Remove from opening set
          openingFiles.remove(filePath);

          LOGGER.info("Opened file in new tab: " + file.getName());
        }
      });
    } catch (Exception e) {
      // Remove from opening set on error
      synchronized (ParquetToolWindow.class) {
        openingFiles.remove(filePath);
      }
      LOGGER.error("Error opening file: " + file.getName(), e);
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
      
      // Update tab components for remaining tabs (recreate to fix indices)
      updateTabComponents();
      
      // Update mappings for remaining tabs
      updateTabMappings();
      
      LOGGER.info("Closed tab: " + panel.getDisplayName());
    }
  }

  /**
   * Updates tab components after tab removal to fix close button indices.
   * No longer needed since we're using title-based approach.
   */
  private void updateTabComponents() {
    // Update titles to include close indicator if not already present
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      String currentTitle = tabbedPane.getTitleAt(i);
      if (!currentTitle.endsWith("  ×")) {
        // Remove any existing close indicator and add new one
        String baseTitle = currentTitle.replace("  ×", "").trim();
        tabbedPane.setTitleAt(i, baseTitle + "  ×");
      }
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

