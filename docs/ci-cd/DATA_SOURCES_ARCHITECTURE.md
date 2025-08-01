# 🔄 Arquitectura de Fuentes de Datos - EuroLeague App

## 📊 **Fuentes de Datos Actuales**

### 🎯 **Fuente Principal: EuroLeague Feeds API**
- **URL Base:** `https://feeds.incrowdsports.com/provider/euroleague-feeds/v2`
- **Propósito:** Datos principales de partidos, equipos, estadísticas y calendario
- **Formato:** JSON estructurado
- **Características:**
  - ✅ Datos en tiempo real
  - ✅ API oficial y estable
  - ✅ Cobertura completa de temporada
  - ✅ Actualizaciones automáticas

### 🖼️ **Fuente Secundaria: EuroLeague Website**
- **URL Base:** `https://www.euroleaguebasketball.net`
- **Propósito:** Imágenes de jugadores y contenido complementario
- **Formato:** HTML scraping específico
- **Uso Limitado:**
  - 🎯 Solo para imágenes de jugadores (PlayerImageUtil)
  - 🎯 URLs de referencia cuando sea necesario

---

## 🏗️ **Arquitectura de Acceso a Datos**

### **📡 Data Sources (Capa de Datos)**

#### **1. EuroLeagueJsonApiScraper** ⭐ **Principal**
```kotlin
// Ubicación: data/datasource/remote/scraper/EuroLeagueJsonApiScraper.kt
// Fuente: feeds.incrowdsports.com
// Funcionalidad: Obtención de equipos, partidos, rosters
```

**Métodos principales:**
- `getTeams()` - Equipos con logos desde feeds API
- `getMatches()` - Partidos con resultados en tiempo real
- `getTeamRoster()` - Plantillas de jugadores

#### **2. EuroLeagueRemoteDataSource** 🔄 **Wrapper**
```kotlin
// Ubicación: data/datasource/remote/EuroLeagueRemoteDataSource.kt
// Fuente: Wrapper sobre EuroLeagueJsonApiScraper
// Funcionalidad: Capa adicional con fallbacks
```

**Responsabilidades:**
- Envolver llamadas al JsonApiScraper
- Proveer equipos de fallback si la API falla
- Manejo de errores y logging

#### **3. PlayerImageUtil** 🖼️ **Específico**
```kotlin
// Ubicación: utils/PlayerImageUtil.kt
// Fuente: www.euroleaguebasketball.net (web scraping)
// Funcionalidad: Solo imágenes de jugadores
```

### **🔀 Flujo de Datos**

```
📱 UI Layer
    ↓
🎯 ViewModels
    ↓
📋 Use Cases
    ↓
🗄️ Repositories ────┐
    ↓               ↓
💾 Local DB     📡 Remote Sources
(Room)              ↓
                🌐 EuroLeagueJsonApiScraper (feeds.incrowdsports.com)
                🖼️ PlayerImageUtil (www.euroleaguebasketball.net)
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

## 🎯 **Políticas de Uso por Fuente**

### **✅ Usar Feeds API para:**
- ✅ **Equipos:** Nombres, códigos, países, logos
- ✅ **Partidos:** Fechas, horarios, resultados, estado
- ✅ **Calendarios:** Jornadas, temporadas
- ✅ **Estadísticas:** Puntuaciones, información de partido
- ✅ **Rosters:** Plantillas de jugadores

### **🎯 Usar Website para:**
- 🖼️ **Imágenes:** Solo fotos de jugadores
- 🔗 **Referencias:** URLs complementarias cuando sea necesario

### **❌ NO usar Website para:**
- ❌ **Datos de partidos** (usar feeds API)
- ❌ **Información de equipos** (usar feeds API) 
- ❌ **Calendarios** (usar feeds API)
- ❌ **Estadísticas** (usar feeds API)

---

## 📋 **URLs de Referencia**

### **🔧 Configuración Actual:**

#### **EuroLeagueJsonApiScraper:**
```kotlin
private const val FEEDS_BASE_URL = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2"
private const val GAMES_URL = "$FEEDS_BASE_URL/competitions/E/seasons/E2025/games"
private const val ROSTER_URL = "$FEEDS_BASE_URL/competitions/E/seasons/E2025/clubs"
```

#### **PlayerImageUtil:**
```kotlin
// Solo para imágenes de jugadores
val playerPageUrl = "https://www.euroleaguebasketball.net/euroleague/players/$formattedName/$playerCode/"
```

#### **Profile URLs generadas:**
```kotlin
// Equipos desde feeds API
private fun generateTeamProfileUrl(teamCode: String): String {
    return "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/teams/$teamCode"
}
```

---

## 🔄 **Migración Completada**

### **✅ Cambios Realizados:**
1. **Fuente principal:** Migrado a feeds.incrowdsports.com
2. **URLs de equipos:** Actualizadas para usar feeds API
3. **Documentación:** Clarificada separación de fuentes
4. **Comentarios:** Especificado propósito de cada fuente

### **📋 Archivos Actualizados:**
- `EuroLeagueJsonApiScraper.kt` - URLs actualizadas a feeds API
- `PlayerImageUtil.kt` - Documentado uso específico del website
- `README.md` - Fuentes de datos clarificadas
- Este documento - Arquitectura completa documentada

### **🎯 Estado Final:**
- ✅ **Consistencia:** Una fuente principal (feeds API) para datos
- ✅ **Claridad:** Uso específico y documentado del website
- ✅ **Mantenibilidad:** Arquitectura clara y documentada
- ✅ **Performance:** API rápida y confiable como fuente principal

---

**Fecha de última actualización:** 1 de agosto de 2025  
**Versión de la documentación:** 1.0  
**Estado:** ✅ Arquitectura optimizada y consolidada
