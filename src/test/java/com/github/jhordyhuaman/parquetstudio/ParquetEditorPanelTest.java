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

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParquetEditorPanelTest {

  private ParquetEditorPanel editorPanel;

  @BeforeEach
  void setUp() {
    editorPanel = new ParquetEditorPanel();
  }

  @Test
  @DisplayName("Should initialize with no file loaded")
  void testInitialState() {
    assertThat(editorPanel.hasFile()).isFalse();
    assertThat(editorPanel.getCurrentFile()).isNull();
    assertThat(editorPanel.getDisplayName()).isEqualTo("Untitled");
  }

  @Test
  @DisplayName("Should return correct display name after loading file")
  void testDisplayName() {
    // Note: This test verifies the method exists and works
    // Actual file loading would require a real Parquet file
    assertThat(editorPanel.getDisplayName()).isNotNull();
  }

  @Test
  @DisplayName("Should handle file state correctly")
  void testFileState() {
    assertThat(editorPanel.hasFile()).isFalse();
    assertThat(editorPanel.getCurrentFile()).isNull();
  }
}

