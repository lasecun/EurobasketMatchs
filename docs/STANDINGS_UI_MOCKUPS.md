# 📱 Mockups UI - Clasificación EuroLeague

## Opción 1: Tabs en MainScreen

```
┌─────────────────────────────────────────┐
│  🏀 EuroLeague 2026          [⚙️]      │
├─────────────────────────────────────────┤
│  ┌─────────┐  ┌──────────────┐         │
│  │ Partidos│  │ Clasificación│         │
│  └─────────┘  └──────────────┘         │
├─────────────────────────────────────────┤
│  📊 Temporada Regular 2025-26           │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Pos  Equipo            J  G  P  % │ │
│  ├───────────────────────────────────┤ │
│  │ 1  🏆 Real Madrid     10  8  2 80 │ │
│  │ 2  ⭐ FC Barcelona    10  7  3 70 │ │
│  │ 3    Panathinaikos    10  7  3 70 │ │
│  │ 4    Fenerbahce       10  6  4 60 │ │
│  │ 5    Olympiacos        9  5  4 56 │ │
│  │ 6    AS Monaco         9  5  4 56 │ │
│  │ 7    Anadolu Efes      9  5  4 56 │ │
│  │ 8    Partizan          9  5  4 56 │ │
│  │ ...                               │ │
│  └───────────────────────────────────┘ │
│                                         │
│  [Regular Season ▼] [Actualizar 🔄]    │
└─────────────────────────────────────────┘
```

**Flujo de Navegación:**
```
MainScreen → [Tab Clasificación] → Ver tabla
                                  → [Tap equipo] → TeamRosterScreen
```

---

## Opción 4: Bottom Navigation Bar (RECOMENDADA)

### Vista 1: Pantalla de Partidos
```
┌─────────────────────────────────────────┐
│  🏀 Partidos                   [⚙️]     │
├─────────────────────────────────────────┤
│  📅  Sábado, 7 Diciembre 2025    [📆]  │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │  Real Madrid    [🏀]    Barça       ││
│  │      78                   82        ││
│  │  🔴 FINALIZADO - Q4 Jornada 12     ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │  Panathinaikos  [🏀]    Fenerbahce ││
│  │      91                   88        ││
│  │  🔴 FINALIZADO - Q4 Jornada 12     ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │  Olympiacos    [🏀]    Monaco       ││
│  │      --                   --        ││
│  │  ⏰ 20:30 Jornada 12               ││
│  └─────────────────────────────────────┘│
├═════════════════════════════════════════┤
│  🏀        📊         ⭐         ⚙️     │
│ Partidos  Clasif.  Favoritos   Más     │
└─────────────────────────────────────────┘
```

### Vista 2: Pantalla de Clasificación
```
┌─────────────────────────────────────────┐
│  📊 Clasificación                       │
├─────────────────────────────────────────┤
│  Temporada 2025-26                      │
│  ┌──────────────┐  ┌───────────┐       │
│  │Regular Season│  │ Playoffs  │       │
│  └──────────────┘  └───────────┘       │
├─────────────────────────────────────────┤
│  ┌────┬────────────────┬──┬──┬──┬────┐ │
│  │Pos │ Equipo         │ J│ G│ P│  % │ │
│  ├────┼────────────────┼──┼──┼──┼────┤ │
│  │ 🥇 │ 🏀 Real Madrid │10│ 8│ 2│ 80 │ │
│  │  1 │ +45 dif        │  │  │  │    │ │
│  ├────┼────────────────┼──┼──┼──┼────┤ │
│  │ 🥈 │ ⭐ Barça       │10│ 7│ 3│ 70 │ │
│  │  2 │ +32 dif        │  │  │  │    │ │
│  ├────┼────────────────┼──┼──┼──┼────┤ │
│  │ 🥉 │ 🏀 Panathinaik.│10│ 7│ 3│ 70 │ │
│  │  3 │ +28 dif        │  │  │  │    │ │
│  ├────┼────────────────┼──┼──┼──┼────┤ │
│  │  4 │ 🏀 Fenerbahce  │10│ 6│ 4│ 60 │ │
│  │    │ +15 dif        │  │  │  │    │ │
│  ├────┼────────────────┼──┼──┼──┼────┤ │
│  │  5 │ 🏀 Olympiacos  │ 9│ 5│ 4│ 56 │ │
│  │    │ +8 dif         │  │  │  │    │ │
│  ├────┼────────────────┼──┼──┼──┼────┤ │
│  │  6 │ 🏀 AS Monaco    │ 9│ 5│ 4│ 56 │ │
│  │    │ +5 dif         │  │  │  │    │ │
│  └────┴────────────────┴──┴──┴──┴────┘ │
│                                         │
│  ℹ️ Tap en un equipo para ver detalles │
│                                         │
│  Última actualización: Hoy 22:45       │
│                               [🔄]      │
├═════════════════════════════════════════┤
│  🏀        📊         ⭐         ⚙️     │
│ Partidos  Clasif.  Favoritos   Más     │
└─────────────────────────────────────────┘
```

### Vista 3: Detalles de Equipo (desde clasificación)
```
┌─────────────────────────────────────────┐
│  ← Real Madrid                          │
├─────────────────────────────────────────┤
│           ┌─────────────┐               │
│           │             │               │
│           │   [LOGO]    │               │
│           │             │               │
│           └─────────────┘               │
│                                         │
│  🏆 Real Madrid                         │
│  📍 Madrid, España                      │
│  👤 Chus Mateo                         │
├─────────────────────────────────────────┤
│  📊 Estadísticas Temporada              │
│  ┌────────────────────────────────────┐ │
│  │ Posición:      #1                  │ │
│  │ Jugados:       10                  │ │
│  │ Ganados:       8                   │ │
│  │ Perdidos:      2                   │ │
│  │ % Victoria:    80%                 │ │
│  │ Dif. Puntos:   +45                 │ │
│  └────────────────────────────────────┘ │
│                                         │
│  ┌────────────────────────────────────┐ │
│  │  👥 Ver Plantilla del Equipo       │ │
│  └────────────────────────────────────┘ │
│                                         │
│  🏀 Próximos Partidos                  │
│  ┌────────────────────────────────────┐ │
│  │ 10 Dic - vs Barça                  │ │
│  │ 15 Dic - @ Panathinaikos           │ │
│  └────────────────────────────────────┘ │
├═════════════════════════════════════════┤
│  🏀        📊         ⭐         ⚙️     │
│ Partidos  Clasif.  Favoritos   Más     │
└─────────────────────────────────────────┘
```

---

## Componentes UI Necesarios

### 1. StandingRow.kt
```kotlin
@Composable
fun StandingRow(
    position: Int,
    team: Team,
    standing: Standing,
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición con medalla para top 3
            Box(modifier = Modifier.width(40.dp)) {
                if (position <= 3) {
                    Text(medal(position), fontSize = 20.sp)
                } else {
                    Text(position.toString(), style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            // Logo del equipo
            AsyncImage(
                model = team.logoUrl,
                contentDescription = team.name,
                modifier = Modifier.size(32.dp)
            )
            
            // Nombre del equipo
            Text(
                text = team.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal
            )
            
            // Estadísticas
            StatsChip(standing.played.toString())
            StatsChip(standing.won.toString())
            StatsChip(standing.lost.toString())
            Text(
                text = "${standing.winPercentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Segunda fila con diferencia de puntos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Spacer(Modifier.width(40.dp))
            Text(
                text = "Diferencia: ${standing.pointsDifference.withSign()}",
                style = MaterialTheme.typography.bodySmall,
                color = if (standing.pointsDifference > 0) 
                    Color.Green else Color.Red
            )
        }
    }
}
```

### 2. SeasonTypeSelector.kt
```kotlin
@Composable
fun SeasonTypeSelector(
    selectedType: SeasonType,
    onTypeSelected: (SeasonType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == SeasonType.REGULAR,
            onClick = { onTypeSelected(SeasonType.REGULAR) },
            label = { Text("Temporada Regular") },
            modifier = Modifier.weight(1f)
        )
        
        FilterChip(
            selected = selectedType == SeasonType.PLAYOFFS,
            onClick = { onTypeSelected(SeasonType.PLAYOFFS) },
            label = { Text("Playoffs") },
            modifier = Modifier.weight(1f)
        )
    }
}
```

---

## Estados de la UI

### Loading State
```
┌─────────────────────────────────────────┐
│  📊 Clasificación                       │
├─────────────────────────────────────────┤
│                                         │
│           ⏳                            │
│                                         │
│     Cargando clasificación...           │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

### Empty State
```
┌─────────────────────────────────────────┐
│  📊 Clasificación                       │
├─────────────────────────────────────────┤
│                                         │
│           📊                            │
│                                         │
│  No hay datos de clasificación          │
│                                         │
│  La clasificación se actualizará        │
│  automáticamente con los resultados     │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │      🔄 Sincronizar ahora        │  │
│  └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### Error State
```
┌─────────────────────────────────────────┐
│  📊 Clasificación                       │
├─────────────────────────────────────────┤
│                                         │
│           ⚠️                            │
│                                         │
│  Error al cargar clasificación          │
│                                         │
│  No se pudo obtener la información      │
│  de clasificación. Por favor,           │
│  inténtalo de nuevo.                    │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │         🔄 Reintentar            │  │
│  └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

---

## Interacciones

### 1. Pull to Refresh
```
Usuario desliza hacia abajo
    ↓
Aparece indicador de carga
    ↓
Se sincroniza la clasificación
    ↓
Se actualiza la tabla
    ↓
Animación de actualización
```

### 2. Tap en Equipo
```
Usuario toca fila de equipo
    ↓
Animación de ripple
    ↓
Navega a TeamRosterScreen
    ↓
Muestra plantilla del equipo
```

### 3. Cambio de Temporada
```
Usuario toca "Playoffs"
    ↓
Animación de transición
    ↓
Se cargan datos de playoffs
    ↓
Se actualiza la tabla
```

---

## Animaciones

### Entrada de Filas (Staggered)
```kotlin
LazyColumn {
    itemsIndexed(standings) { index, standing ->
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                ),
                initialOffsetY = { it * (index + 1) }
            ) + fadeIn()
        ) {
            StandingRow(
                position = index + 1,
                standing = standing,
                // ...
            )
        }
    }
}
```

### Transición entre Tabs
```kotlin
AnimatedContent(
    targetState = selectedSeasonType,
    transitionSpec = {
        slideInHorizontally { width -> width } + fadeIn() with
        slideOutHorizontally { width -> -width } + fadeOut()
    }
) { seasonType ->
    StandingsTable(seasonType)
}
```

---

## Accesibilidad

### Descripciones de Contenido
- Logo del equipo: "Logo de {nombreEquipo}"
- Posición: "Posición {número} en la clasificación"
- Estadísticas: "{ganados} victorias de {jugados} partidos jugados"
- Favorito: "Equipo favorito: {nombreEquipo}"

### Tamaños de Tap
- Mínimo 48dp x 48dp para todas las áreas interactivas
- Espaciado adecuado entre elementos

### Contraste de Colores
- Texto sobre fondo: ratio mínimo 4.5:1
- Indicadores de estado con colores y texto

---

## Notas de Implementación

### Performance
- Usar `LazyColumn` para listas grandes
- Cachear logos de equipos
- Paginación si hay más de 20 equipos

### Estado
- Mantener estado en ViewModel
- Sobrevivir a cambios de configuración
- Sincronizar con repositorio local

### Testing
- Unit tests para ViewModel
- UI tests para interacciones
- Screenshot tests para diseño

---

**Fecha**: 8 de Diciembre 2025  
**Versión**: 1.0  
**Estado**: Diseño Propuesto

