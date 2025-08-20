# Manual Testing Guide - Static Data System

## Overview
Esta guía proporciona pasos para verificar manualmente que el sistema de datos estáticos funciona correctamente en la aplicación EuroLeague.

## Prerequisites
- App compilada y ejecutándose
- Datos estáticos en `app/src/main/assets/static_data/`
- Conexión a internet para sincronización dinámica

## Test Cases

### 1. Static Data Loading
**Objetivo**: Verificar que los datos estáticos se cargan correctamente al iniciar la app.

**Pasos**:
1. Instalar app nueva (datos locales vacíos)
2. Abrir la aplicación
3. Observar el comportamiento de inicialización

**Resultado esperado**:
- La app debe cargar rápidamente sin indicadores de sincronización largos
- Los equipos deben aparecer inmediatamente
- El calendario debe mostrar partidos programados

### 2. Smart Sync Card
**Objetivo**: Verificar que el SmartSyncCard funciona correctamente.

**Pasos**:
1. Abrir pantalla principal
2. Localizar el SmartSyncCard debajo del selector de fecha
3. Verificar que muestra estado actual de sincronización
4. Tocar botón "Verificar" para buscar actualizaciones
5. Tocar botón "Sincronizar" para forzar sincronización

**Resultado esperado**:
- Card debe mostrar último tiempo de sincronización
- Botones deben responder adecuadamente
- Estados deben actualizarse correctamente

### 3. Fallback Mechanism
**Objetivo**: Verificar que el fallback al sistema tradicional funciona.

**Pasos**:
1. Renombrar temporalmente carpeta `assets/static_data/` 
2. Reinstalar app
3. Abrir aplicación
4. Observar comportamiento

**Resultado esperado**:
- App debe detectar falta de datos estáticos
- Debe caer al sistema de sincronización tradicional
- Debe cargar datos desde la red

### 4. Dynamic Data Sync
**Objetivo**: Verificar que solo se sincronizan datos dinámicos.

**Pasos**:
1. Con app funcionando normalmente
2. Usar SmartSyncCard para sincronizar manualmente
3. Observar logs para verificar qué datos se sincronizan

**Resultado esperado**:
- Solo deben sincronizarse marcadores y estados de partidos
- No deben re-cargarse equipos ni calendario completo
- Sincronización debe ser rápida

### 5. Favorite Teams Integration
**Objetivo**: Verificar que equipos favoritos funcionan con datos estáticos.

**Pasos**:
1. Ir al calendario
2. Marcar algunos equipos como favoritos
3. Verificar indicadores dorados en partidos
4. Volver a pantalla principal
5. Verificar que partidos de favoritos se destacan

**Resultado esperado**:
- Equipos favoritos deben identificarse correctamente
- Indicadores visuales deben aparecer
- Funcionalidad debe ser consistente entre pantallas

## Performance Verification

### Cold Start Time
**Test**: Medir tiempo desde apertura hasta datos visibles
- **Con datos estáticos**: < 2 segundos
- **Sin datos estáticos (fallback)**: 5-10 segundos

### Network Usage
**Test**: Monitorear uso de red durante la primera carga
- **Con datos estáticos**: Mínimo (solo scores actuales)
- **Sin datos estáticos**: Alto (equipos + calendario + scores)

### Memory Usage
**Test**: Verificar uso de memoria
- **Expectativa**: Sin aumentos significativos vs. versión anterior

## Error Scenarios

### 1. Corrupted Static Data
**Setup**: Modificar archivos JSON en assets para que sean inválidos
**Expected**: App debe detectar error y usar fallback

### 2. Network Unavailable
**Setup**: Deshabilitar conexión de red
**Expected**: Datos estáticos deben cargar normalmente, sync dinámico debe fallar gracefully

### 3. Partial Data
**Setup**: Eliminar solo uno de los archivos estáticos
**Expected**: App debe detectar datos incompletos y usar fallback

## Logging Verification

Buscar en logs los siguientes mensajes:
- `✅ Datos estáticos inicializados`
- `⚠️ Falló inicialización estática, usando método tradicional`
- `🔄 Iniciando sincronización manual...`
- `📊 Analytics: static_data_initialized`

## Success Criteria

El sistema funciona correctamente si:
1. ✅ Carga inicial es rápida con datos estáticos
2. ✅ Fallback funciona cuando datos estáticos fallan
3. ✅ SmartSyncCard responde correctamente
4. ✅ Sincronización dinámica solo actualiza scores
5. ✅ Equipos favoritos funcionan correctamente
6. ✅ No hay crashes ni errores visibles
7. ✅ Performance es mejor que versión anterior

## Troubleshooting

### App no carga datos estáticos
- Verificar que archivos existen en `assets/static_data/`
- Revisar logs para errores de parsing JSON
- Verificar que ManageStaticDataUseCase está inyectado

### SmartSyncCard no aparece
- Verificar import en MainScreen
- Revisar que MainViewModel expone states correctos
- Verificar integración en MainScreen layout

### Fallback no funciona
- Verificar que syncDataUseCase sigue funcionando
- Revisar lógica de error handling en initializeApp()
- Verificar que checkAndSyncData() aún existe

---

*Este documento debe actualizarse conforme evolucione el sistema.*
