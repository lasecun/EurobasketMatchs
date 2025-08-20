# Datos Estáticos de EuroLeague

Este directorio contiene scripts para poblar automáticamente los datos estáticos de EuroLeague 2025-26, permitiendo que la aplicación funcione con datos precargados desde la primera instalación.

## 🎯 Objetivo

Cuando el usuario instale la aplicación, debe tener todos los equipos y partidos ya cargados en datos estáticos, cumpliendo el requisito:

> "Cuando instalo la aplicación debería tener todos los partidos ya cargados en static data"

## 📁 Archivos

### Scripts de Poblado

- **`populate_game_center_data.py`** - Script principal en Python que extrae datos de:
  - API de clubs para información completa de equipos (colores, países, venues, etc.)
  - Game Center de EuroLeague para partidos de jornadas disponibles
  - API de feeds como respaldo para partidos adicionales

- **`populate_static_data.sh`** - Script bash que ejecuta el proceso completo

### Datos Generados

- **`app/src/main/assets/static_data.json`** - Archivo JSON con todos los datos estáticos:
  - 20 equipos con información rica (colores, países, ciudades, venues, etc.)
  - Partidos disponibles combinados de múltiples fuentes
  - Metadatos de temporada y fecha de actualización

## 🚀 Uso

### Poblado Automático

```bash
# Desde el directorio raíz del proyecto
./scripts/populate_static_data.sh
```

### Poblado Manual

```bash
# Ejecutar directamente el script Python
python3 scripts/populate_game_center_data.py
```

## 📊 Datos Incluidos

### Equipos (20)
Cada equipo incluye:
- **Información básica**: ID, nombre, código
- **Colores**: Primario y secundario en formato hex
- **Ubicación**: País, ciudad, venue
- **Contacto**: Website, teléfono, dirección
- **Social**: Twitter, URL de tickets
- **Organización**: Presidente

Ejemplo:
```json
{
  "id": "BAR",
  "name": "FC Barcelona",
  "primaryColor": "#004d9f",
  "secondaryColor": "#c4122e",
  "country": "Spain",
  "city": "Barcelona",
  "venue": "Palau Blaugrana",
  "website": "https://www.fcbarcelona.com/",
  "president": "Joan Laporta"
}
```

### Partidos (~60)
Cada partido incluye:
- **Equipos**: IDs de local y visitante
- **Horario**: Fecha y hora en formato ISO
- **Jornada**: Número de round
- **Resultado**: Puntuaciones (si está jugado)
- **Estado**: scheduled, confirmed, played, etc.
- **Venue**: Nombre del pabellón

## 🔄 Fuentes de Datos

### 1. API de Clubs (Primaria para equipos)
```
https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs
```
- ✅ 20 equipos con información completa
- ✅ Colores oficiales, ubicaciones, contactos

### 2. Game Center (Primaria para partidos actuales)
```
https://www.euroleaguebasketball.net/es/euroleague/game-center/?round=X&season=E2025
```
- ✅ Jornadas disponibles con horarios exactos
- ✅ Información de venues actualizada
- ⚠️ Solo jornadas programadas (actualmente jornada 1)

### 3. API de Feeds (Respaldo para partidos)
```
https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/games
```
- ✅ 50 partidos más recientes
- ⚠️ Limitado por paginación

## 🏗️ Integración con la App

### Ubicación de Datos
Los datos estáticos se guardan en:
```
app/src/main/assets/static_data.json
```

### Uso en el Código
El sistema híbrido utiliza estos datos:

1. **StaticDataManager** lee el archivo de assets
2. **SmartSyncManager** convierte datos estáticos a modelos de dominio
3. **ManageStaticDataUseCase** proporciona lógica de negocio
4. **StaticDataGenerator** puede generar/actualizar datos cuando sea necesario

### Flujo de Datos
```
Instalación → Datos Estáticos → Funcionamiento Inmediato
                    ↓
              Actualización Manual → API Live → Datos Frescos
```

## 🔧 Mantenimiento

### Actualización de Datos
Para actualizar los datos estáticos:

```bash
# Ejecutar el script de poblado
./scripts/populate_static_data.sh

# Verificar los datos generados
ls -la app/src/main/assets/static_data.json
```

### Validación
El script incluye validaciones automáticas:
- ✅ Verificación de conexión a APIs
- ✅ Validación de estructura JSON
- ✅ Estadísticas de datos obtenidos
- ✅ Ejemplos de equipos procesados

### Monitoreo
El script muestra información detallada:
- Número de equipos obtenidos
- Número de partidos por fuente
- Jornadas disponibles
- Ejemplos de datos procesados

## 📈 Estadísticas Actuales

**Última ejecución exitosa:**
- 🏆 **Equipos**: 20 con información completa
- ⚽ **Partidos**: 60 combinados de múltiples fuentes
- 📅 **Jornadas**: 1, 34, 35, 36, 37, 38
- 🎯 **Cobertura**: Funcionamiento inmediato al instalar

## 🔍 Troubleshooting

### Error: "No se pudieron obtener datos"
- Verificar conexión a internet
- Comprobar que las URLs de API estén disponibles
- Revisar logs para errores específicos

### Error: "Python3 no está instalado"
```bash
# En macOS
brew install python3

# En Ubuntu/Debian
sudo apt-get install python3
```

### Datos incompletos
- El script combina múltiples fuentes para maximizar datos
- Las jornadas futuras se agregan automáticamente cuando estén disponibles
- El sistema funciona con los datos disponibles

## 🎉 Resultado

Al ejecutar exitosamente el poblado:

1. **Instalación inmediata**: Los usuarios ven equipos y partidos sin esperar
2. **Experiencia rica**: Colores de equipos, países, venues disponibles
3. **Funcionamiento offline**: La app funciona sin conexión inicial
4. **Actualización opcional**: El usuario puede refrescar cuando quiera

¡La aplicación estará lista para ofrecer una experiencia completa desde el primer momento!
