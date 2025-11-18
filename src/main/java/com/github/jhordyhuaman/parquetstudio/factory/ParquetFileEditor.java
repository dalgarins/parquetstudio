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
package com.github.jhordyhuaman.parquetstudio.factory;

import com.github.jhordyhuaman.parquetstudio.ui.ParquetToolWindow;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * File editor for Parquet files.
 * Opens the Parquet Studio tool window and loads the file when a .parquet file is opened.
 */
public class ParquetFileEditor extends UserDataHolderBase implements FileEditor {
  private final Project project;
  private final VirtualFile file;
  private volatile boolean fileOpened = false;

  public ParquetFileEditor(@NotNull Project project, @NotNull VirtualFile file) {
    this.project = project;
    this.file = file;
    
    // Open Parquet Studio tool window and load the file
    openInParquetStudio();
  }

  private void openInParquetStudio() {
    // Prevent multiple openings
    if (fileOpened) {
      return;
    }
    
    // Use invokeLater to ensure the tool window is available
    javax.swing.SwingUtilities.invokeLater(() -> {
      try {
        // Double-check to prevent race conditions
        if (fileOpened) {
          return;
        }
        
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("Parquet Studio");
        
        if (toolWindow != null) {
          toolWindow.activate(() -> {
            // Get the content and find ParquetToolWindow
            javax.swing.JComponent content = toolWindow.getComponent();
            if (content != null) {
              ParquetToolWindow parquetToolWindow = findParquetToolWindowRecursive(content);
              
              if (parquetToolWindow != null) {
                java.io.File physicalFile = new java.io.File(file.getPath());
                parquetToolWindow.openFileInTab(physicalFile);
                fileOpened = true;
              }
            }
          }, true);
        }
      } catch (Exception e) {
        com.intellij.openapi.diagnostic.Logger.getInstance(ParquetFileEditor.class)
            .warn("Error opening file in Parquet Studio: " + e.getMessage());
      }
    });
  }

  @Nullable
  private ParquetToolWindow findParquetToolWindowRecursive(@NotNull javax.swing.JComponent component) {
    if (component instanceof ParquetToolWindow) {
      return (ParquetToolWindow) component;
    }
    
    for (int i = 0; i < component.getComponentCount(); i++) {
      java.awt.Component child = component.getComponent(i);
      if (child instanceof javax.swing.JComponent) {
        ParquetToolWindow found = findParquetToolWindowRecursive((javax.swing.JComponent) child);
        if (found != null) {
          return found;
        }
      }
    }
    
    return null;
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    // Return an empty panel - the actual UI is in the tool window
    // This prevents showing unnecessary content in the editor tab
    javax.swing.JPanel panel = new javax.swing.JPanel();
    panel.setPreferredSize(new java.awt.Dimension(0, 0));
    return panel;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return null;
  }

  @NotNull
  @Override
  public String getName() {
    return "Parquet Studio";
  }

  @NotNull
  @Override
  public VirtualFile getFile() {
    return file;
  }

  @Override
  public void setState(@NotNull FileEditorState state) {
    // No state to set
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public boolean isValid() {
    return file.isValid();
  }

  @Override
  public void selectNotify() {
    // Only open if not already opened (to prevent multiple openings)
    if (!fileOpened) {
      openInParquetStudio();
    }
  }

  @Override
  public void deselectNotify() {
    // Nothing to do
  }

  @Override
  public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    // No properties to listen to
  }

  @Override
  public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    // No properties to listen to
  }

  @Nullable
  @Override
  public FileEditorLocation getCurrentLocation() {
    return null;
  }

  @Override
  public void dispose() {
    // Nothing to dispose
  }
}

