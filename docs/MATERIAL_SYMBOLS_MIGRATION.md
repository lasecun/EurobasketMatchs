# 🎨 Guía de Migración a Material Symbols

## 📢 Aviso Importante de Google

Google ha **descontinuado** la librería `androidx.compose.material.icons` y recomienda migrar a **Material Symbols**:

> "The androidx.compose.material.icons library is no longer recommended for displaying Material Icons in Compose, as Material Symbols are the new way forward. We have stopped publishing updates to this library and it has been removed from the latest Material 3 library release."

## 🚨 Problemas de la Librería Actual

1. **No se actualiza**: Google ya no publica actualizaciones
2. **Iconos obsoletos**: No incluye el nuevo estilo Material Symbols
3. **Build times lentos**: Incluye TODOS los iconos aunque no se usen
4. **Removida de Material 3**: Ya no viene con las últimas versiones

## ✅ Solución Temporal Implementada

Para que la app compile ahora, hemos usado solo iconos disponibles en `material-icons-core`:

```kotlin
BottomNavigationItem(
    Matches -> Icons.Filled.Home / Icons.Outlined.Home
    Standings -> Icons.AutoMirrored.Filled.List / Icons.AutoMirrored.Outlined.List  
    Favorites -> Icons.Filled.Star / Icons.Filled.Star
    More -> Icons.Filled.MoreVert / Icons.Outlined.MoreVert
)
```

**Nota**: Star usa el mismo icono para selected/unselected porque `Icons.Outlined.Star` no existe en la librería core.

## 🎯 Migración Recomendada a Material Symbols

### Paso 1: Descargar Iconos desde Google Fonts

1. Ir a https://fonts.google.com/icons
2. Buscar el icono deseado (ej: "sports basketball")
3. En el panel derecho, seleccionar el tab **"Android"**
4. Configurar el estilo:
   - **Optical size**: 24dp
   - **Weight**: 400 (Regular) o 300 (Light for outlined)
   - **Grade**: 0
   - **Fill**: 1 (filled) o 0 (outlined)
5. Click en **"Download Android XML"**

### Paso 2: Crear Carpeta de Iconos Personalizados

```
app/src/main/res/drawable/
├── ic_basketball.xml           # Icono de baloncesto filled
├── ic_basketball_outlined.xml  # Icono de baloncesto outlined
├── ic_leaderboard.xml          # Clasificación filled
├── ic_leaderboard_outlined.xml # Clasificación outlined
├── ic_star.xml                 # Estrella filled
├── ic_star_outlined.xml        # Estrella outlined
└── ic_more_vert.xml            # Menú filled
```

### Paso 3: Ejemplo de Icono Vectorial (XML)

```xml
<!-- ic_basketball.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="960"
    android:viewportHeight="960"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M480,880q-83,0 -156,-31.5T197,763q-54,-54 -85.5,-127T80,480q0,-83 31.5,-156T197,197q54,-54 127,-85.5T480,80q83,0 156,31.5T763,197q54,54 85.5,127T880,480q0,83 -31.5,156T763,763q-54,54 -127,85.5T480,880Z"/>
</vector>
```

### Paso 4: Crear Iconos en Kotlin

Crea un archivo `CustomIcons.kt` con tus iconos vectoriales:

```kotlin
// app/src/main/java/es/itram/basketmatch/ui/icons/CustomIcons.kt
package es.itram.basketmatch.ui.icons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object CustomIcons {
    
    val Basketball: ImageVector
        @Composable
        get() {
            return remember {
                ImageVector.Builder(
                    name = "basketball",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f
                ).apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1.0f,
                        stroke = null,
                        strokeAlpha = 1.0f,
                        strokeLineWidth = 1.0f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Miter,
                        strokeLineMiter = 1.0f,
                        pathFillType = PathFillType.NonZero
                    ) {
                        // Path data aquí - copiar del XML descargado
                        moveTo(12f, 2f)
                        // ... más comandos de path
                    }
                }.build()
            }
        }
    
    val Leaderboard: ImageVector
        @Composable
        get() {
            return remember {
                ImageVector.Builder(
                    name = "leaderboard",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f
                ).apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1.0f
                    ) {
                        // Path data del icono de clasificación
                        moveTo(7f, 14f)
                        // ... más comandos
                    }
                }.build()
            }
        }
}
```

### Paso 5: Actualizar BottomNavigationItem

```kotlin
// Opción A: Usar recursos de drawable
object Matches : BottomNavigationItem(
    route = NavigationRoutes.MAIN,
    selectedIcon = painterResource(R.drawable.ic_basketball),
    unselectedIcon = painterResource(R.drawable.ic_basketball_outlined),
    label = "Partidos"
)

// Opción B: Usar iconos personalizados de Kotlin
object Matches : BottomNavigationItem(
    route = NavigationRoutes.MAIN,
    selectedIcon = CustomIcons.Basketball,
    unselectedIcon = CustomIcons.BasketballOutlined,
    label = "Partidos"
)
```

## 🔧 Herramientas Útiles

### 1. Convertidor SVG a Compose

Para convertir SVG/XML a código Compose:
- https://www.composables.com/svgtocompose
- https://svg2vector.com/

### 2. Material Symbols Explorer

- https://fonts.google.com/icons
- Filtrar por "Material Symbols"
- Descargar en formato Android XML

### 3. Plugin Android Studio

- **Material Design Icons Exporter**: Plugin para importar Material Symbols directamente

## 📋 Plan de Migración Completo

### Fase 1: Identificar Iconos Necesarios ✅
- [x] Home (Partidos)
- [x] List (Clasificación)
- [x] Star (Favoritos)
- [x] MoreVert (Más)

### Fase 2: Descargar Material Symbols
- [ ] Basketball/Sports (para Partidos)
- [ ] Leaderboard/EmojiEvents (para Clasificación)
- [ ] Star/StarOutline (para Favoritos)
- [ ] MoreVert (para Más)

### Fase 3: Implementar Iconos Personalizados
- [ ] Crear carpeta `app/src/main/res/drawable/`
- [ ] Añadir archivos XML de iconos
- [ ] O crear `CustomIcons.kt` con ImageVector

### Fase 4: Actualizar BottomNavigationItem
- [ ] Cambiar de `Icons.Filled.X` a `CustomIcons.X`
- [ ] O usar `painterResource(R.drawable.ic_x)`

### Fase 5: Remover Dependencia (Opcional)
- [ ] Remover `material-icons-core` de `build.gradle.kts`
- [ ] Limpiar imports no usados

## 📦 Iconos Recomendados para la App

| Sección | Icono Material Symbol | Alternativa |
|---------|----------------------|-------------|
| **Partidos** | `sports_basketball` | `sports`, `home` |
| **Clasificación** | `leaderboard` | `emoji_events`, `table_chart` |
| **Favoritos** | `star` / `star_outline` | `favorite` / `favorite_border` |
| **Más** | `more_vert` | `menu`, `settings` |

## 💡 Ventajas de Material Symbols

1. **✅ Actualizados**: Nuevo estilo visual moderno
2. **⚡ Más rápido**: Solo incluyes los iconos que usas
3. **🎨 Personalizable**: Puedes ajustar weight, grade, optical size
4. **📱 Consistente**: Mismo estilo que Material 3
5. **🔄 Mantenido**: Google sigue actualizando Material Symbols

## 🔗 Enlaces Útiles

- **Material Symbols**: https://fonts.google.com/icons
- **Guía oficial**: https://m3.material.io/styles/icons/overview
- **Android Docs**: https://developer.android.com/develop/ui/views/graphics/vector-drawable-resources
- **Compose Icons Guide**: https://developer.android.com/develop/ui/compose/graphics/images/customize

## ⚠️ Notas Importantes

1. **No mezclar**: No uses `material-icons` y Material Symbols juntos
2. **Tamaño consistente**: Usa 24dp para todos los iconos
3. **Tint**: Los iconos XML deben usar `android:tint="?attr/colorControlNormal"`
4. **Accessibility**: Añade `contentDescription` a todos los iconos

## 🎯 Estado Actual del Proyecto

### ✅ Funcionando con Icons Core
- Compilación OK
- Iconos básicos funcionando
- Navigation operativa

### 🔄 Pendiente
- Migrar a Material Symbols para iconos más apropiados
- Especialmente: Icono de baloncesto para Partidos
- Icono de clasificación/tabla para Standings

### 📝 Siguiente Paso Recomendado

1. Descargar los 4 iconos necesarios desde Google Fonts
2. Colocarlos en `res/drawable/`
3. Actualizar `BottomNavigationItem.kt` para usar `painterResource()`
4. Verificar que se ven correctamente

---

**Fecha**: 8 de Diciembre 2025  
**Referencia**: Google Material Symbols Migration  
**Estado**: Guía completa para migración

