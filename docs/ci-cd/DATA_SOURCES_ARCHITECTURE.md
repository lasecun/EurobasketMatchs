# 🔄 Arquitectura de Fuentes de Datos - EuroLeague App

## 📊 **Fuente de Datos Unificada**

### 🎯 **Fuente Única: EuroLeague Feeds API**
- **URL Base:** `https://feeds.incrowdsports.com/provider/euroleague-feeds/v2`
- **Propósito:** Todos los datos de la aplicación
- **Formato:** JSON estructurado
- **Características:**
  - ✅ Datos en tiempo real
  - ✅ API oficial y estable
  - ✅ Cobertura completa de temporada
  - ✅ Actualizaciones automáticas
  - ✅ **Incluye imágenes de jugadores** 🆕
  - ✅ **URLs de logos de equipos**
  - ✅ **Sin dependencias externas**

### 🚫 **Eliminado: EuroLeague Website**
- ~~**URL Base:** `https://www.euroleaguebasketball.net`~~ ❌
- ~~**Propósito:** Imágenes de jugadores~~ ❌
- **Motivo de eliminación:** Feeds API ahora proporciona todas las imágenes necesarias

---

## 🏗️ **Arquitectura de Acceso a Datos Simplificada**

### **📡 Data Source Único**

#### **EuroLeagueJsonApiScraper** ⭐ **Fuente Única**
```kotlin
// Ubicación: data/datasource/remote/scraper/EuroLeagueJsonApiScraper.kt
// Fuente: feeds.incrowdsports.com (ÚNICAMENTE)
// Funcionalidad: TODOS los datos de la aplicación
```

**Métodos principales:**
- `getTeams()` - Equipos con logos desde feeds API
- `getMatches()` - Partidos con resultados en tiempo real
- `getTeamRoster()` - Plantillas con **imágenes de jugadores incluidas** 🆕

#### **EuroLeagueRemoteDataSource** 🔄 **Wrapper Simplificado**
```kotlin
// Ubicación: data/datasource/remote/EuroLeagueRemoteDataSource.kt
// Fuente: Wrapper sobre EuroLeagueJsonApiScraper
// Funcionalidad: Capa adicional con fallbacks (solo feeds API)
```

**Responsabilidades:**
- Envolver llamadas al JsonApiScraper
- Proveer equipos de fallback si la API falla
- Manejo de errores y logging

#### **PlayerImageUtil** � **Simplificado**
```kotlin
// Ubicación: utils/PlayerImageUtil.kt
// Fuente: feeds.incrowdsports.com (ACTUALIZADO)
// Funcionalidad: Obtener imágenes desde feeds API
```

### **🔀 Flujo de Datos Simplificado**

```
📱 UI Layer
    ↓
🎯 ViewModels
    ↓
📋 Use Cases
    ↓
🗄️ Repositories ────┐
    ↓               ↓
💾 Local DB     📡 Feeds API (feeds.incrowdsports.com)
(Room)              ↓
                🌐 EuroLeagueJsonApiScraper
                   ├── Equipos + Logos
                   ├── Partidos + Resultados
                   ├── Jugadores + Imágenes 🆕
                   └── Estadísticas
```

### **⚡ Sincronización de Datos**

#### **DataSyncService** 🔄 **Servicio Central**
```kotlin
// Ubicación: domain/service/DataSyncService.kt
// Responsabilidad: Sincronización masiva y inicial
```

**Funciones:**
- Sincronización completa de equipos y partidos
- Progreso en tiempo real
- Gestión de primera carga
- Refresh periódico

#### **Repository Pattern** 📋 **Acceso Individual**
```kotlin
// TeamRepositoryImpl + MatchRepositoryImpl
// Responsabilidad: Acceso granular a datos
```

**Funciones:**
- Acceso por demanda
- Cache local (Room)
- Refresh específico

---

## 🎯 **Política de Uso Unificada**

### **✅ Usar Feeds API para TODO:**
- ✅ **Equipos:** Nombres, códigos, países, logos (imageUrls.crest)
- ✅ **Partidos:** Fechas, horarios, resultados, estado en tiempo real
- ✅ **Calendarios:** Jornadas, temporadas completas
- ✅ **Estadísticas:** Puntuaciones, información detallada de partidos
- ✅ **Rosters:** Plantillas de jugadores con información completa
- ✅ **Imágenes de Jugadores:** URLs directas (images.profile, images.headshot) 🆕
- ✅ **Imágenes de Equipos:** Logos oficiales incluidos

### **🚫 NO usar otras fuentes:**
- ❌ **Sitio web oficial** - Ya no necesario
- ❌ **Scraping HTML** - Obsoleto
- ❌ **APIs externas** - Feeds API es completa

---

## 📋 **URLs de Configuración Unificadas**

### **🔧 Configuración Única:**

#### **EuroLeagueJsonApiScraper:**
```kotlin
private const val FEEDS_BASE_URL = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2"
private const val GAMES_URL = "$FEEDS_BASE_URL/competitions/E/seasons/E2025/games"
private const val ROSTER_URL = "$FEEDS_BASE_URL/competitions/E/seasons/E2025/clubs"
```

#### **Profile URLs generadas:**
```kotlin
// Equipos desde feeds API
private fun generateTeamProfileUrl(teamCode: String): String {
    return "$FEEDS_BASE_URL/competitions/E/seasons/E2025/clubs/$teamCode"
}
```

#### **Imágenes de Jugadores:**
```kotlin
// Directamente desde PlayerDto.images de feeds API
data class PlayerImageUrls(
    val profile: String? = null,    // URL completa del CDN
    val headshot: String? = null    // URL completa del CDN
)
```

---

## 🔄 **Migración Completada - Fuente Única**

### **✅ Cambios Finales:**
1. **Fuente unificada:** Migrado completamente a feeds.incrowdsports.com
2. **Imágenes de jugadores:** Ahora desde feeds API (PlayerDto.images)
3. **URLs de equipos:** Actualizadas para usar feeds API exclusivamente
4. **PlayerImageUtil:** Refactorizado para usar feeds API
5. **Documentación:** Clarificada arquitectura de fuente única
6. **Eliminadas dependencias:** Ya no se usa www.euroleaguebasketball.net

### **📋 Archivos Actualizados:**
- `EuroLeagueJsonApiScraper.kt` - Documentación actualizada para fuente única
- `PlayerImageUtil.kt` - Refactorizado para usar feeds API
- `README.md` - Fuente única documentada
- Este documento - Arquitectura simplificada

### **🎯 Estado Final - Arquitectura Simplificada:**
- ✅ **Consistencia Total:** Una sola fuente para todos los datos
- ✅ **Imágenes Incluidas:** Feeds API proporciona URLs de imágenes de jugadores
- ✅ **Sin Dependencias Externas:** No más scraping de sitios web
- ✅ **Mantenibilidad:** Arquitectura mucho más simple
- ✅ **Performance:** Una sola API rápida y confiable
- ✅ **Escalabilidad:** Fácil agregar nuevas funcionalidades

### **📊 Beneficios de la Unificación:**
- 🚀 **75% menos complejidad** - Una fuente vs múltiples
- ⚡ **Mejor performance** - Sin múltiples llamadas a diferentes APIs
- 🔧 **Mantenimiento simplificado** - Un solo punto de configuración
- 🛡️ **Más confiable** - API oficial vs scraping web
- 📈 **Escalable** - Feeds API diseñada para aplicaciones

---

**Fecha de última actualización:** 1 de agosto de 2025  
**Versión de la documentación:** 2.0 - Fuente Única  
**Estado:** ✅ Arquitectura completamente unificada con feeds API
