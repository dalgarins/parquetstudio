# Guía de Iconos - Parquet Studio

Guía rápida para usar iconos en Parquet Studio.

## 📍 Dónde Descargar

**Sitio oficial**: https://intellij-icons.jetbrains.design/

Busca y descarga los iconos que necesites. Cada icono tiene versiones para tema claro y oscuro.

## 📁 Estructura de Archivos

```
src/main/resources/icons/
├── parquet_studio.svg          # Icono principal del plugin
└── ui/                         # Iconos de la interfaz
    └── nombreIcono/
        ├── nombreIcono.svg     # Tema claro
        └── nombreIcono_dark.svg # Tema oscuro
```

## 💻 Uso en el Código

Usa `IconLoader` para cargar iconos con soporte automático de temas:

```java
import com.intellij.openapi.util.IconLoader;

// Ejemplo: Botón de búsqueda
Icon searchIcon = IconLoader.getIcon(
    "/icons/ui/search/search.svg", 
    ParquetEditorPanel.class
);
JButton searchButton = new JButton(searchIcon);
searchButton.setToolTipText("Search");
```

`IconLoader` automáticamente selecciona el icono correcto según el tema activo.

## ✅ Checklist para Agregar un Icono

1. Descargar desde https://intellij-icons.jetbrains.design/
2. Crear carpeta en `icons/ui/` con nombre camelCase
3. Colocar ambos archivos: `nombre.svg` y `nombre_dark.svg`
4. Usar `IconLoader.getIcon()` en el código
5. Agregar tooltip al componente

## 📚 Recursos

- [IntelliJ Icons Website](https://intellij-icons.jetbrains.design/)
- [Icon Style Guidelines](https://plugins.jetbrains.com/docs/intellij/icons-style.html)
