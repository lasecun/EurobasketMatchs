# 📸 Implementación de Imágenes de Jugadores desde API de Euroleague

**Fecha:** 18 de Octubre de 2025  
**Estado:** ✅ Completado

---

## 📋 Resumen

Se ha implementado exitosamente la carga de imágenes de jugadores desde la **API oficial de Euroleague** en lugar de usar el scraper web. La implementación es más estable, rápida y confiable.

---

## 🎯 Problema Inicial

Los jugadores aparecían **sin imágenes** cuando se accedía al detalle de un equipo. El repositorio `TeamRosterRepositoryImpl` estaba generando datos ficticios en lugar de usar la API real de Euroleague.

---

## ✅ Solución Implementada

### 1. **API Oficial de Euroleague**

Se configuró el uso de la API oficial para obtener el roster de equipos:

**Endpoint:**
```
GET /v2/competitions/E/seasons/E2025/clubs/{clubCode}/people
```

**Respuesta:** Array directo de jugadores (sin wrapper `data`)

### 2. **Estructura de Datos**

#### PlayerDto (API oficial)
```kotlin
data class PlayerDto(
    val code: String,
    val name: String?,                    // Nullable - algunos jugadores no tienen nombre
    val firstName: String? = null,
    val lastName: String? = null,
    val imageUrls: PlayerImageUrlsDto? = null,  // 📸 Imágenes del jugador
    val position: String? = null,
    val height: String? = null,
    val birthDate: String? = null,
    val country: CountryDto? = null,
    val dorsal: String? = null            // String para evitar NumberFormatException
) {
    val dorsalNumber: Int? get() = dorsal?.toIntOrNull()
    val validName: String get() = name ?: firstName ?: lastName ?: code
}
```

#### PlayerImageUrlsDto
```kotlin
data class PlayerImageUrlsDto(
    val profile: String? = null,    // URL imagen de perfil
    val headshot: String? = null    // URL imagen headshot
)
```

### 3. **Estrategia de Obtención de Imágenes**

El sistema intenta obtener la imagen del jugador en este orden de prioridad:

1. **`imageUrls.profile`** - Imagen de perfil desde la API oficial ✅ PRINCIPAL
2. **`imageUrls.headshot`** - Imagen headshot desde la API oficial
3. **Web scraping** - Intenta obtener desde el sitio oficial de Euroleague
4. **Placeholder** - Genera un avatar con las iniciales del jugador

```kotlin
val imageUrl = profileImage 
    ?: headshotImage 
    ?: getPlayerImageFromWeb(dto.code, playerName)
    ?: generatePlaceholderImageUrl(playerName)
```

### 4. **Manejo Robusto de Errores**

Se implementaron múltiples capas de seguridad para manejar datos inconsistentes de la API:

#### Problema 1: Array directo vs objeto con campo `data`
- **Error:** `Expected BEGIN_OBJECT but was BEGIN_ARRAY`
- **Solución:** Cambiar DTO a `typealias TeamRosterResponseDto = List<PlayerDto>`

#### Problema 2: Campos numéricos vacíos
- **Error:** `NumberFormatException: empty String`
- **Solución:** Cambiar `dorsal` de `Int?` a `String?` con propiedad computada `dorsalNumber`

#### Problema 3: Nombres null
- **Error:** `NullPointerException: Parameter specified as non-null is null`
- **Solución:** 
  - Hacer `name` nullable
  - Agregar propiedad `validName` que siempre devuelve un valor válido
  - Mejorar `generatePlaceholderImageUrl` para manejar cualquier string

```kotlin
private fun generatePlaceholderImageUrl(playerName: String): String {
    if (playerName.isBlank()) {
        return "https://ui-avatars.com/api/?name=??&size=400&background=004996&color=ffffff&font-size=0.4"
    }
    
    val initials = try {
        playerName.trim()
            .split(" ", "-", "_")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifEmpty { "??" }
    } catch (e: Exception) {
        "??"
    }
    
    return "https://ui-avatars.com/api/?name=$initials&size=400&background=004996&color=ffffff&font-size=0.4"
}
```

---

## 📁 Archivos Modificados

### 1. `EuroLeagueApiDtos.kt`
- ✅ Cambió `TeamRosterResponseDto` a typealias
- ✅ Hizo `name` nullable en `PlayerDto`
- ✅ Cambió `dorsal` de `Int?` a `String?`
- ✅ Agregó propiedades computadas `dorsalNumber` y `validName`

### 2. `EuroLeagueOfficialApiDataSource.kt`
- ✅ Agregó método `getTeamRoster()` con reintentos automáticos
- ✅ Logging detallado para debugging
- ✅ Estadísticas de jugadores con/sin imágenes

### 3. `PlayerMapper.kt`
- ✅ Agregó función `fromApiDto()` para mapear desde API oficial
- ✅ Mejoró `generatePlaceholderImageUrl()` con manejo robusto de errores
- ✅ Estrategia de fallback para imágenes

### 4. `TeamRosterRepositoryImpl.kt`
- ✅ Cambió de `EuroLeagueJsonApiScraper` a `EuroLeagueOfficialApiDataSource`
- ✅ Usa `PlayerMapper.fromApiDto()` para conversión
- ✅ Mantiene sistema de caché inteligente (24 horas)

### 5. `EuroLeagueApiMapper.kt`
- ✅ Agregó `toSimplePlayer()` usando `validName`
- ✅ Maneja imágenes desde `imageUrls`

---

## 🔄 Flujo de Datos

```
Usuario → Detalle Equipo
    ↓
TeamRosterViewModel
    ↓
TeamRosterRepository
    ↓
¿Cache válido?
    ├── SÍ → Devolver desde cache local (SQLite)
    └── NO → Obtener desde API
        ↓
    EuroLeagueOfficialApiDataSource
        ↓
    GET /v2/competitions/E/seasons/E2025/clubs/{clubCode}/people
        ↓
    PlayerDto[] (con imageUrls)
        ↓
    PlayerMapper.fromApiDto()
        ↓
    Player (modelo dominio)
        ├── Con imagen desde API
        ├── Con imagen desde web scraping
        └── Con placeholder
        ↓
    Guardar en cache local
        ↓
    Mostrar en UI
```

---

## 📊 Resultados Esperados

### En Logs
```
✅ Roster obtenido: 48 personas para PAM
📸 Primer jugador válido: John Doe
📸 imageUrls=PlayerImageUrlsDto(profile=https://..., headshot=null)
📸 profile=https://..., headshot=null
📊 Con imágenes: 15, Sin imágenes: 33
✅ Roster obtenido y guardado en cache para PAM (15 jugadores)
```

### En la App
- ✅ Jugadores cargados con sus datos reales
- ✅ Imágenes de perfil cuando están disponibles en la API
- ✅ Avatares con iniciales como fallback elegante
- ✅ Sin errores ni crashes
- ✅ Experiencia de usuario fluida

---

## 🎨 Ejemplo de Placeholder

Para jugadores sin imagen, se genera un avatar con fondo azul Euroleague (#004996):

```
https://ui-avatars.com/api/?name=JD&size=400&background=004996&color=ffffff&font-size=0.4
```

![Avatar Example](https://ui-avatars.com/api/?name=JD&size=400&background=004996&color=ffffff&font-size=0.4)

---

## ⚡ Ventajas de Usar la API Oficial

| Aspecto | Scraper Web | API Oficial |
|---------|-------------|-------------|
| **Estabilidad** | ⚠️ Puede romperse con cambios en el sitio | ✅ Estable y versionada |
| **Velocidad** | 🐢 Lento (parsing HTML) | ⚡ Rápido (JSON) |
| **Confiabilidad** | ⚠️ Depende de la estructura HTML | ✅ Contrato de API oficial |
| **Mantenimiento** | 🔧 Requiere actualizaciones frecuentes | ✅ Mínimo mantenimiento |
| **Datos** | ⚠️ Puede tener inconsistencias | ✅ Datos oficiales de Euroleague |

---

## 🧪 Testing

### Validación Manual
1. Abrir la app
2. Navegar a "Equipos" o "Partidos"
3. Hacer clic en el logo de un equipo
4. Verificar que se muestran los jugadores con sus imágenes

### Logs a Verificar
```bash
# Buscar en logcat:
adb logcat | grep -E "TeamRosterRepository|EuroLeagueOfficialApi|PlayerMapper"
```

**Logs exitosos:**
- ✅ "Roster obtenido: X personas"
- ✅ "Con imágenes: X, Sin imágenes: Y"
- ✅ "Roster obtenido y guardado en cache"

**Logs de error a evitar:**
- ❌ NumberFormatException
- ❌ NullPointerException
- ❌ Expected BEGIN_OBJECT but was BEGIN_ARRAY

---

## 🔮 Mejoras Futuras

1. **Caché de Imágenes**
   - Usar Coil o Glide para cachear imágenes descargadas
   - Reducir consumo de datos

2. **Actualización Automática**
   - Sincronizar rosters cuando hay nuevos fichajes
   - Notificaciones push para cambios

3. **Fallback Mejorado**
   - Si la API falla, usar datos en caché aunque estén expirados
   - Modo offline más robusto

4. **Imágenes de Entrenadores**
   - Implementar carga de imágenes para staff técnico
   - Diferenciar visualmente jugadores vs entrenadores

5. **Optimización de Red**
   - Implementar paginación si hay muchos jugadores
   - Cargar imágenes solo cuando son visibles (lazy loading)

---

## 📚 Referencias

- **API de Euroleague:** https://api-live.euroleague.net/swagger/
- **Documentación Swagger:** Version V2
- **Endpoint de Roster:** `/v2/competitions/{competitionCode}/seasons/{seasonCode}/clubs/{clubCode}/people`

---

## ✅ Checklist de Implementación

- [x] Configurar endpoint de API oficial
- [x] Crear DTOs para la respuesta de la API
- [x] Implementar mapper de PlayerDto a Player
- [x] Actualizar repositorio para usar API oficial
- [x] Manejar errores de datos inconsistentes
- [x] Implementar estrategia de fallback para imágenes
- [x] Agregar logging detallado
- [x] Probar con múltiples equipos
- [x] Validar caché funciona correctamente
- [x] Documentar la implementación

---

**Estado Final:** ✅ Implementación completada y funcionando correctamente

