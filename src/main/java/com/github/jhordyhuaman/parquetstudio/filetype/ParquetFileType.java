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
package com.github.jhordyhuaman.parquetstudio.filetype;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * File type for Parquet files (.parquet).
 * This allows IntelliJ IDEA to recognize .parquet files and associate them with Parquet Studio.
 */
public class ParquetFileType extends LanguageFileType {
  public static final ParquetFileType INSTANCE = new ParquetFileType();

  private ParquetFileType() {
    super(ParquetLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Parquet";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Parquet file";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "parquet";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return IconLoader.getIcon("/icons/parquet_studio.svg", ParquetFileType.class);
  }
}

