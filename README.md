# EuroLeague 2026 - Aplicación Android

Una aplicación Android moderna para seguir todos los partidos de la EuroLeague de baloncesto 2026, desarrollada con las últimas tecnologías de Android.

## 📋 Descripción

Esta aplicación permite a los usuarios seguir el calendario completo de partidos de la EuroLeague 2026, incluyendo fechas, equipos, resultados, estadísticas y roster de jugadores. Desarrollada siguiendo principios de Clean Architecture y las mejores prácticas de desarrollo Android moderno.

## ✨ Características Principales

### 🏀 **Funcionalidades Core**
- **📅 Calendario de Partidos**: Vista completa de todos los partidos de la temporada
- **👥 Roster de Equipos**: Información detallada de jugadores con posiciones y estadísticas
- **📊 Clasificación**: Tabla de posiciones actualizada en tiempo real
- **🔍 Detalles de Partidos**: Información completa de cada encuentro
- **⭐ Favoritos**: Sistema completo de equipos y jugadores favoritos con persistencia local
- **📱 Widget Nativo**: Widget para pantalla de inicio con partidos del día y equipos favoritos
- **🔔 Notificaciones Push**: Sistema completo con Firebase Cloud Messaging

### 🎯 **Experiencia de Usuario**
- **🌙 Tema Oscuro/Claro**: Interfaz adaptable a las preferencias del usuario
- **📱 Material Design 3**: Diseño moderno siguiendo las guías de Google
- **🔄 Pull-to-Refresh**: Actualización manual de datos
- **📊 Analytics**: Seguimiento completo de uso con Firebase Analytics
- **🛡️ Crashlytics**: Monitoreo automático de errores en tiempo real
- **📱 Offline Mode**: Funcionalidad completa sin conexión
- **🎨 Animaciones Fluidas**: Transiciones suaves entre pantallas
- **⚡ Performance Optimizada**: Cache inteligente y sincronización eficiente

### 🏗️ **Arquitectura y Tecnología**
- **🏛️ Clean Architecture**: Separación clara de responsabilidades
- **🎭 MVVM Pattern**: ViewModels reactivos con StateFlow
- **💉 Dependency Injection**: Hilt para gestión de dependencias
- **🗄️ Base de Datos Local**: Room para cache offline
- **🌐 API Integration**: Datos reales desde la API oficial de EuroLeague
- **🔧 Testing**: Suite completa de tests unitarios e integración
- **🔥 Firebase**: Analytics, Crashlytics y Cloud Messaging integrados

## 🏗️ Arquitectura de la Aplicación

### Arquitectura General

La aplicación sigue los principios de **Clean Architecture** propuesta por Robert C. Martin, organizando el código en capas bien definidas que garantizan la separación de responsabilidades, mantenibilidad y testabilidad.

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │     UI/Views    │  │   ViewModels    │  │   Composables   │ │
│  │   (Activities,  │  │     (State      │  │   (Jetpack      │ │
│  │   Fragments)    │  │   Management)   │  │   Compose)      │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Use Cases     │  │    Entities     │  │   Repositories  │ │
│  │   (Business     │  │   (Domain       │  │   (Interfaces)  │ │
│  │    Logic)       │  │    Models)      │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  Repositories   │  │  Data Sources   │  │    Network      │ │
│  │ (Implementation)│  │  (Local/Remote) │  │    (API)        │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│  ┌─────────────────┐  ┌─────────────────┐                     │
│  │    Database     │  │     DTOs        │                     │
│  │    (Room)       │  │  (Data Models)  │                     │
│  └─────────────────┘  └─────────────────┘                     │
└─────────────────────────────────────────────────────────────┘
```

### Capas de la Arquitectura

#### 1. **Presentation Layer** (UI)
- **Responsabilidad**: Interacción con el usuario, presentación de datos
- **Componentes**:
  - `Activities` y `Fragments` (si es necesario)
  - `@Composable` functions (Jetpack Compose)
  - `ViewModels` (MVVM pattern)
  - `UI State` management
- **Tecnologías**: Jetpack Compose, Material Design 3, Navigation Compose

#### 2. **Domain Layer** (Business Logic)
- **Responsabilidad**: Lógica de negocio, reglas de la aplicación
- **Componentes**:
  - `Entities`: Modelos de dominio (Match, Team, Tournament, etc.)
  - `Use Cases`: Casos de uso específicos (GetMatchesUseCase, GetTeamsUseCase)
  - `Repository Interfaces`: Contratos para acceso a datos
- **Características**: Sin dependencias externas, completamente testeable

#### 3. **Data Layer** (Data Access)
- **Responsabilidad**: Acceso y persistencia de datos
- **Componentes**:
  - `Repository Implementations`: Implementación de interfaces del dominio
  - `Data Sources`: Local (Room) y Remote (API)
  - `DTOs`: Data Transfer Objects para API
  - `Mappers`: Conversión entre DTOs y Entities
- **Tecnologías**: Room Database, Retrofit, OkHttp

### Patrón MVVM

```
View (Composable) ←→ ViewModel ←→ Use Case ←→ Repository
                                      ↓
                                  Data Source
```

## 🎯 Fuentes de Datos

### API de EuroLeague
- **Fuente Única**: [EuroLeague Feeds API](https://feeds.incrowdsports.com/provider/euroleague-feeds/v2) - API oficial completa con todos los datos necesarios
- **Datos disponibles**:
  - Calendario de partidos 2026
  - Información de equipos participantes
  - Resultados en tiempo real
  - Estadísticas de jugadores y equipos
  - Clasificaciones y posiciones

### Datos Locales
- **Base de datos local**: Room Database
- **Caché**: Para funcionamiento offline
- **Sincronización**: Actualización periódica desde la API

## 🛠️ Stack Tecnológico

### Frontend & UI
- **🎨 UI Framework**: Jetpack Compose (BOM 2024.09.00)
- **🎭 Design System**: Material Design 3
- **🧭 Navigation**: Navigation Compose 2.8.4
- **🖼️ Image Loading**: Coil 2.7.0
- **🌈 Theming**: Dynamic Color (Material You)

### Arquitectura & Gestión de Estado
- **🏛️ Pattern**: MVVM + Clean Architecture
- **💉 Dependency Injection**: Hilt 2.48 (Dagger)
- **🔄 State Management**: StateFlow, Compose State
- **⚡ Reactive Programming**: Kotlin Coroutines + Flow

### Datos & Networking
- **🗄️ Local Database**: Room 2.6.1
- **🌐 Network**: Retrofit 2.11.0 + OkHttp 4.12.0
- **📄 Web Scraping**: Jsoup 1.18.1
- **🔗 Serialization**: Kotlinx Serialization 1.6.3 + Gson 2.11.0

### Analytics & Monitoring
- **📊 Analytics**: Firebase Analytics (✅ Configurado y activo)
- **🛡️ Crash Reporting**: Firebase Crashlytics (✅ Configurado y funcional)
- **📈 Performance**: Jetpack Compose Metrics
- **🔧 Error Tracking**: Monitoreo automático de crashes en producción

### Testing & Quality
- **🧪 Unit Tests**: JUnit 4.13.2 + MockK 1.13.13
- **🎭 UI Tests**: Compose Testing + Espresso 3.6.1
- **🔍 Integration Tests**: Room Testing + Turbine
- **📏 Static Analysis**: Detekt + Android Lint

### Build System & CI/CD
- **⚙️ Build System**: Gradle 9.0.0 (Kotlin DSL)
- **🏗️ AGP**: Android Gradle Plugin 8.12.0
- **🐘 Kotlin**: 2.0.21 (K2 Compiler)
- **🚀 CI/CD**: GitHub Actions
- **📋 Code Coverage**: Jacoco

## 📱 Estado de Funcionalidades

### ✅ **Implementado y Funcional**

#### 🏀 **Core Features**
- **📅 Calendario de Partidos**: Vista completa con datos reales de EuroLeague
- **👥 Roster de Equipos**: Información detallada de jugadores por equipo
- **🏟️ Detalles de Partidos**: Información completa de encuentros
- **📊 Datos en Tiempo Real**: Web scraping desde la API oficial

#### 🎨 **UI/UX Moderno**  
- **🌙 Material Design 3**: Interfaz moderna con tema claro/oscuro
- **📱 Jetpack Compose**: UI declarativa y reactiva
- **🧭 Navigation**: Navegación fluida entre pantallas
- **🔄 Pull-to-Refresh**: Actualización manual de datos

#### 🏗️ **Arquitectura Sólida**
- **🏛️ Clean Architecture**: Separación clara de responsabilidades
- **💉 Hilt**: Inyección de dependencias automática
- **🗄️ Room Database**: Cache local con sincronización
- **📊 Firebase Analytics**: Seguimiento de uso y métricas

#### 🧪 **Testing Comprehensive**
- **📈 Cobertura de Tests**: 194 tests implementados
- **🔬 Unit Tests**: Tests para ViewModels, Repositories, Mappers
- **⚡ Integration Tests**: Tests de base de datos y API
- **🎭 UI Tests**: Tests de Compose y navegación

### 🚧 **En Desarrollo**
- **⭐ Sistema de Favoritos**: Equipos y jugadores favoritos
- **🔔 Notificaciones**: Alertas para partidos importantes
- **📊 Estadísticas Avanzadas**: Gráficos y análisis detallados
- **📱 Widgets**: Widget de partidos en la pantalla principal

### 📋 **Roadmap Futuro**
- **🏆 Playoff Tracker**: Seguimiento especial de eliminatorias
- **📈 Player Comparison**: Comparador de estadísticas
- **🎮 Fantasy Mode**: Modo fantasy league
- **🌍 Multi-idioma**: Soporte para múltiples idiomas
- **Accesibilidad**: Cumplimiento de estándares de accesibilidad

### 🎯 **Objetivos 2025**
- 🚀 Lanzamiento de versión beta en producción
- 📱 Implementación de widgets para pantalla principal
- 🔔 Sistema completo de notificaciones push
- 🌍 Soporte multi-idioma (ES/EN/FR)
- 🏆 Modo playoff especial con bracket interactivo
- ⚡ Optimización de rendimiento y UX

### 🔮 **Roadmap Próximas Features**
- **🎮 Fantasy Mode**: Sistema de liga fantasy personalizable
- **📈 Analytics Avanzados**: Dashboard de estadísticas detalladas
- **👥 Social Features**: Compartir y comentar partidos
- **🎯 Predicciones**: Sistema de predicciones con IA
- **🌐 API Propia**: Exposición de datos para terceros

---

**📱 Versión**: 1.1 (versionCode: 3)
**📅 Última actualización**: Septiembre 2025
**🏗️ Build**: Gradle 9.0.0 + AGP 8.12.0  
**🔧 Kotlin**: 2.0.21 (K2 Compiler)  
**🛡️ Crashlytics**: ✅ Activo y monitoreando  
**📊 Analytics**: ✅ Firebase implementado  

---

## 🏆 **Destacados del Proyecto**

> **🚀 Proyecto en estado de producción** con arquitectura sólida, tests completos y monitoreo en tiempo real.

## 🆕 **Novedades en Versión 1.2**

### ✨ **Nuevas Características**

#### 📧 **Sistema de Contacto Completo**
- **Pantalla de Contacto**: Nueva pantalla accesible desde Configuración
- **Integración Email**: Apertura directa del cliente de email nativo con asunto pre-configurado
- **GitHub Issues**: Enlace directo para reportar bugs y solicitar características
- **Material Design 3**: Interfaz moderna con iconos y componentes actualizados
- **Analytics Integrado**: Seguimiento de interacciones de soporte para métricas de usuario

#### 🏗️ **Mejoras Técnicas**
- **Clean Architecture**: Implementación completa con Repository pattern
- **ContactRepository**: Gestión de información de contacto con inyección de dependencias
- **ContactViewModel**: Estado reactivo con StateFlow y manejo de eventos
- **Enhanced Analytics**: Método `trackError` añadido al AnalyticsManager para mejor seguimiento de errores
- **Navegación Optimizada**: Eliminación de conflictos de navegación y archivos duplicados

#### 🔧 **Mejoras de Desarrollo**
- **Error Handling**: Sistema mejorado de manejo y tracking de errores
- **Material Icons**: Corrección de iconos faltantes (Help, Code, ArrowForward)
- **Testing Coverage**: Todos los tests unitarios siguen pasando (51/51)
- **Code Quality**: Sin regresiones en funcionalidad existente

### 📊 **Información de Contacto**
- **Email**: itramgames@gmail.com
- **GitHub Issues**: https://github.com/lasecun/EurobasketMatchs/issues
- **Tiempo de Respuesta**: 24-48 horas
- **Soporte**: Consultas generales, reportes de bugs, solicitudes de características

### 🎯 **Experiencia de Usuario Mejorada**
- **Acceso Fácil**: Contacto disponible desde el menú de configuración
- **Flujo Intuitivo**: Navegación clara con indicadores de carga
- **Integración Nativa**: Uso del cliente de email del dispositivo
- **Feedback Visual**: Estados de carga y confirmaciones de acciones

---

**📱 Versión**: 1.2 (versionCode: 4)  
**📅 Última actualización**: Septiembre 2025  
**🏗️ Build**: Gradle 9.0.0 + AGP 8.12.0  
**🔧 Kotlin**: 2.0.21 (K2 Compiler)  
**🛡️ Crashlytics**: ✅ Activo y monitoreando  
**📊 Analytics**: ✅ Firebase implementado

---

### 🔧 **Para Desarrolladores**
```bash
# Setup rápido
git clone <repo-url>
./gradlew clean build
./gradlew test  # ✅ 194 tests pasando
```

### 📱 **Para Usuarios**
- **Interfaz moderna** con Material Design 3
- **Datos reales** desde API oficial de EuroLeague
- **Modo offline** completamente funcional
- **Monitoreo automático** de errores con Crashlytics

### 🎯 **Para Stakeholders**
- **✅ Build estable** - Sin fallos en producción
- **📊 Analytics activos** - Métricas de uso en tiempo real
- **🛡️ Error tracking** - Detección proactiva de issues
- **🧪 Testing robusto** - 194 tests garantizan calidad

---

> 💡 **¿Preguntas?** Consulta la [documentación completa](./docs/INDEX.md) o revisa los [logs de cambios](./docs/ci-cd/)
