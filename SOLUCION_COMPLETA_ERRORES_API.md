# 🔧 SOLUCIÓN COMPLETA - Errores de Parsing API EuroLeague

## 📋 Resumen de Problemas Encontrados

La aplicación fallaba al obtener partidos de la API oficial de EuroLeague debido a **3 problemas de parsing**:

### ❌ Error 1: Campo `round` inconsistente
```
Expected BEGIN_OBJECT but was NUMBER at line 1 column 461 path $.data[0].round
```

### ❌ Error 2: Campo `gameState` null
```
Attempt to invoke virtual method 'java.lang.String GameStateDto.getCode()' on a null object reference
```

### ❌ Error 3: Campos críticos null
```
Parameter specified as non-null is null: method MatchWebDto.<init>, parameter id
```

---

## ✅ Soluciones Implementadas

### 1️⃣ Solución Error `round` (Número vs Objeto)

**Problema:** La API devuelve `round` de dos formas diferentes:
- Como número: `"round": 5`
- Como objeto: `"round": {"number": 5, "name": "Round 5"}`

**Solución:** Creado `RoundDtoAdapter.kt` - TypeAdapter de Gson personalizado

```kotlin
class RoundDtoAdapter : JsonDeserializer<RoundDto?> {
    override fun deserialize(...): RoundDto? {
        return when {
            // Caso 1: Es un número simple
            json?.isJsonPrimitive == true && json.asJsonPrimitive.isNumber -> {
                val number = json.asInt
                RoundDto(number = number, name = "Round $number")
            }
            
            // Caso 2: Es un objeto completo
            json?.isJsonObject == true -> {
                val jsonObject = json.asJsonObject
                val number = jsonObject.get("number")?.asInt ?: 0
                val name = jsonObject.get("name")?.asString
                RoundDto(number = number, name = name)
            }
            
            // Caso 3: null o cualquier otro caso
            else -> null
        }
    }
}
```

**Registrado en `EuroLeagueApiModule.kt`:**
```kotlin
val gson = GsonBuilder()
    .registerTypeAdapter(RoundDto::class.java, RoundDtoAdapter())
    .create()
```

---

### 2️⃣ Solución Error `gameState` null

**Problema:** La API devuelve `gameState` como `null` en algunos partidos.

**Solución:** Hacer el campo nullable en `GameApiDto`:

```kotlin
// Antes:
@SerialName("gameState") val gameState: GameStateDto,

// Ahora:
@SerialName("gameState") val gameState: GameStateDto? = null,
```

**Actualización en `EuroLeagueApiMapper.kt`:**
```kotlin
// Uso seguro con operador ?.
val mappedStatus = this.gameState?.toMatchStatus() ?: MatchStatus.SCHEDULED
```

---

### 3️⃣ Solución Error Campos Críticos null

**Problema:** Algunos partidos vienen con `code`, `date`, `local` o `road` como `null`.

**Solución:** 

#### A) Hacer campos nullable en `GameApiDto`:
```kotlin
@Serializable
data class GameApiDto(
    @SerialName("code") val code: String?,           // Era: String
    @SerialName("date") val date: String?,           // Era: String
    @SerialName("local") val local: GameTeamDto?,   // Era: GameTeamDto
    @SerialName("road") val road: GameTeamDto?,     // Era: GameTeamDto
    // ...
)
```

#### B) Validar en `EuroLeagueApiMapper.kt`:
```kotlin
fun GameApiDto.toMatchWebDto(): MatchWebDto? {  // Ahora devuelve nullable
    // Validar campos críticos
    val gameCode = this.code
    val gameDate = this.date
    val localTeam = this.local
    val roadTeam = this.road
    
    if (gameCode == null || gameDate == null || localTeam == null || roadTeam == null) {
        android.util.Log.w("ApiMapper", "⚠️ Partido con datos incompletos ignorado")
        return null  // Filtrar partido inválido
    }
    
    // Continuar con mapping normal...
    return MatchWebDto(...)
}
```

#### C) Filtrar nulls en `toMatchWebDtoList()`:
```kotlin
fun List<GameApiDto>.toMatchWebDtoList(): List<MatchWebDto> {
    return this.mapNotNull { it.toMatchWebDto() }  // Usa mapNotNull
}
```

---

## 📁 Archivos Modificados

### Creados:
1. ✅ **`RoundDtoAdapter.kt`** - Adaptador personalizado de Gson

### Modificados:
1. ✅ **`EuroLeagueApiModule.kt`** - Registró el adaptador en Gson
2. ✅ **`EuroLeagueApiDtos.kt`** - Campos nullable en `GameApiDto`
3. ✅ **`EuroLeagueApiMapper.kt`** - Validaciones y manejo de nulls

---

## 🚀 Cómo Aplicar

### 1. Clean + Rebuild (IMPORTANTE)
```
Build → Clean Project
Build → Rebuild Project
```

### 2. Desinstalar la App Completamente
```
Settings → Apps → Eurobasket → Uninstall
```
**Importante:** Esto limpia la caché y garantiza que se use el código nuevo.

### 3. Ejecutar la App
```
Run → Run 'app'
```

---

## 📊 Resultado Esperado

Ahora los logs deberían mostrar:

```
🏀 Obteniendo partidos temporada E2025 usando API v2...
--> GET https://api-live.euroleague.net/v2/competitions/E/seasons/E2025/games
<-- 200 OK ✅

✅ Partidos obtenidos desde API: 306
⚠️ Partido con datos incompletos ignorado: code=null, date=2025-10-15  (si hay alguno)
📊 Estados de partidos: {SCHEDULED=280, FINISHED=26}

🔍 Iniciando enriquecimiento de 306 partidos...
📊 Resumen: Finalizados=26, Programados=280, En vivo=0

🔹 1: Estado=FINISHED, Fecha=2025-10-03
📊 ✅ PARTIDO FINALIZADO 1 - Obteniendo marcador real...
🔍 Partido: 1
   Estado RAW: 'FINISHED' - 'Final'
   Boxscore local: 89
   Boxscore road: 84
✅ Marcadores finales: 89 - 84
✅ 1: Real Madrid 89 - 84 Panathinaikos Athens

🔹 2: Estado=FINISHED, Fecha=2025-10-03
📊 ✅ PARTIDO FINALIZADO 2 - Obteniendo marcador real...
✅ 2: Fenerbahce 82 - 76 Olympiacos
...
```

**Sin errores de parsing** ✅

---

## 🎯 Resumen de Cambios

| Problema | Solución | Archivo |
|----------|----------|---------|
| `round` como número | `RoundDtoAdapter` | `RoundDtoAdapter.kt` (nuevo) |
| `round` registro | `registerTypeAdapter()` | `EuroLeagueApiModule.kt` |
| `gameState` null | Campo nullable + `?.` | `EuroLeagueApiDtos.kt` + `EuroLeagueApiMapper.kt` |
| Campos críticos null | Nullable + validación | `EuroLeagueApiDtos.kt` + `EuroLeagueApiMapper.kt` |
| Filtrar inválidos | `mapNotNull()` | `EuroLeagueApiMapper.kt` |

---

## 🛡️ Beneficios de la Solución

✅ **Robusto:** Maneja datos inconsistentes de la API  
✅ **Seguro:** No crashea con datos null o inesperados  
✅ **Filtrado:** Ignora partidos con datos incompletos  
✅ **Logs:** Registra partidos problemáticos para debugging  
✅ **Compatible:** Funciona con todas las variantes de respuesta de la API  

---

## 📝 Notas Técnicas

- El proyecto usa **Gson** para parsing JSON (no Kotlinx Serialization)
- Los TypeAdapters de Gson permiten deserialización personalizada
- `mapNotNull()` filtra automáticamente los elementos null de listas
- El operador `?.` (safe call) previene NullPointerException
- Los logs ayudan a identificar partidos problemáticos sin crashear la app

---

## ⚠️ Si Aún Hay Errores

Si después de aplicar los cambios aún ves errores:

1. **Verificar que realmente hiciste Clean + Rebuild**
2. **Verificar que desinstalaste la app** (no solo reinstalar)
3. **Revisar Logcat** para ver si hay otros campos null
4. **Revisar el JSON de la API** para identificar nuevos campos problemáticos

Para revisar el JSON completo de un partido específico:
```bash
curl "https://api-live.euroleague.net/v2/competitions/E/seasons/E2025/games" | jq '.'
```

