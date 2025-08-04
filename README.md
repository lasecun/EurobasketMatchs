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
- **⭐ Favoritos**: Marca tus equipos y jugadores favoritos

### 🎯 **Experiencia de Usuario**
- **🌙 Tema Oscuro/Claro**: Interfaz adaptable a las preferencias del usuario
- **📱 Material Design 3**: Diseño moderno siguiendo las guías de Google
- **🔄 Pull-to-Refresh**: Actualización manual de datos
- **📊 Analytics**: Seguimiento de uso con Firebase Analytics
- **🛡️ Crashlytics**: Monitoreo de errores en tiempo real

### 🏗️ **Arquitectura y Tecnología**
- **🏛️ Clean Architecture**: Separación clara de responsabilidades
- **🎭 MVVM Pattern**: ViewModels reactivos con StateFlow
- **💉 Dependency Injection**: Hilt para gestión de dependencias
- **🗄️ Base de Datos Local**: Room para cache offline
- **🌐 Web Scraping**: Datos reales desde la API oficial de EuroLeague

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
- **📊 Analytics**: Firebase Analytics
- **🛡️ Crash Reporting**: Firebase Crashlytics
- **📈 Performance**: Jetpack Compose Metrics

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

## 🧪 Testing Strategy

### Pirámide de Testing
```
        E2E Tests (UI)
           ↗️       ↖️
    Integration    Component
       Tests         Tests
         ↗️             ↖️
      Unit Tests ←→ Unit Tests
     (Domain)      (Presentation)
```

### Tipos de Tests
1. **Unit Tests** (70%)
   - Use Cases
   - ViewModels
   - Mappers
   - Utilities

2. **Integration Tests** (20%)
   - Repository implementations
   - Database operations
   - API calls

3. **E2E Tests** (10%)
   - Flujos críticos de usuario
   - Navegación completa
   - Escenarios de error

## 📦 Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/ch/biketec/t/
│   │   │   ├── presentation/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── matches/
│   │   │   │   │   ├── teams/
│   │   │   │   │   ├── standings/
│   │   │   │   │   └── common/
│   │   │   │   ├── viewmodel/
│   │   │   │   └── navigation/
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   ├── usecase/
│   │   │   │   └── repository/
│   │   │   ├── data/
│   │   │   │   ├── repository/
│   │   │   │   ├── datasource/
│   │   │   │   │   ├── local/
│   │   │   │   │   └── remote/
│   │   │   │   ├── dto/
│   │   │   │   └── mapper/
│   │   │   └── di/
│   │   └── res/
│   ├── test/ (Unit Tests)
│   └── androidTest/ (Integration & UI Tests)
```

## 🚀 Roadmap de Desarrollo

### Fase 1: Fundamentos (Semanas 1-2)
- [ ] Configuración inicial del proyecto
- [ ] Setup de Clean Architecture
- [ ] Configuración de Hilt
- [ ] Estructura base de navegación

### Fase 2: Datos (Semanas 3-4)
- [ ] Implementación de API client
- [ ] Setup de Room Database
- [ ] Implementación de Repositories
- [ ] Mappers y DTOs

### Fase 3: Domain Logic (Semana 5)
- [ ] Entidades de dominio
- [ ] Use Cases principales
- [ ] Casos de error y validaciones

### Fase 4: UI Principal (Semanas 6-8)
- [ ] Pantalla de calendario
- [ ] Lista de equipos
- [ ] Detalles de partidos
- [ ] Navegación completa

### Fase 5: Features Avanzadas (Semanas 9-10)
- [ ] Modo offline
- [ ] Notificaciones
- [ ] Favoritos
- [ ] Compartir contenido

### Fase 6: Testing & Optimización (Semanas 11-12)
- [ ] Tests unitarios completos
- [ ] Tests de integración
- [ ] Optimización de rendimiento
- [ ] Testing de accesibilidad

## 🔧 Configuración del Entorno

### 📋 **Requisitos del Sistema**
- **🖥️ Android Studio**: Ladybug (2024.2.1) o superior
- **☕ JDK**: OpenJDK 17 (recomendado: JetBrains Runtime)
- **🐘 Kotlin**: 2.0.21+ (K2 Compiler)
- **📱 Android SDK**: API 36 (Android 14+)
- **⚙️ Gradle**: 9.0.0+
- **🏗️ AGP**: 8.12.0+

### 🚀 **Setup del Proyecto**

#### 1. **Clonación e Instalación**
```bash
# Clonar el repositorio
git clone https://github.com/lasecun/EurobasketMatchs.git
cd EurobasketMatchs

# Verificar Gradle
./gradlew --version
# Gradle 9.0.0 ✅

# Sync y build inicial
./gradlew clean assembleDebug
```

#### 2. **Verificación de Tests**
```bash
# Ejecutar tests unitarios
./gradlew test

# Generar reporte de cobertura
./gradlew jacocoTestReport

# Tests de UI (requiere emulador/dispositivo)
./gradlew connectedAndroidTest
```

#### 3. **Firebase Setup** (Opcional)
```bash
# 1. Crear proyecto en Firebase Console
# 2. Agregar google-services.json a app/
# 3. Configurar Analytics y Crashlytics
```

### 🛠️ **Herramientas de Desarrollo**

#### **Análisis de Código**
```bash
# Detekt (análisis estático)
./gradlew detekt

# Lint de Android
./gradlew lint
```

#### **Debugging y Profiling**
- **🔍 Layout Inspector**: Para debugging de UI Compose
- **📊 Compose Metrics**: Análisis de rendimiento
- **🐛 Flipper**: Debugging de network y base de datos

## 📚 Documentación Técnica

### 📖 **Estructura de Documentación**
Toda la documentación técnica está organizada en [`docs/`](./docs/):

- **🚀 Implementation**: [`docs/implementation/`](./docs/implementation/) - Planes y arquitectura
- **🔥 Firebase**: [`docs/firebase/`](./docs/firebase/) - Analytics y configuración
- **🧪 Testing**: [`docs/testing/`](./docs/testing/) - Estrategias de testing
- **🔄 CI/CD**: [`docs/ci-cd/`](./docs/ci-cd/) - Pipelines y automatización
- **🛡️ GitHub**: [`docs/github/`](./docs/github/) - Configuración de repositorio

### 📋 **Enlaces Rápidos**
- [📊 Analytics Implementation](./docs/firebase/FIREBASE_ANALYTICS_IMPLEMENTATION.md)
- [🏗️ Architecture Plan](./docs/implementation/IMPLEMENTATION_PLAN.md)
- [🧪 Testing Strategy](./docs/testing/)
- [🔄 CI/CD Workflows](./docs/ci-cd/WORKFLOWS_OPTIMIZATION_SUMMARY.md)

## 👥 **Desarrollo y Contribución**

### 🎯 **Metodología de Desarrollo**
- **📋 Branching**: Feature branches con PR reviews
- **🧪 Testing**: Cobertura mínima del 80%
- **📝 Code Review**: Obligatorio para todas las PRs
- **📊 Analytics**: Tracking de métricas de desarrollo

### 🏗️ **Build & CI/CD**
```bash
# Build modes disponibles
./gradlew assembleDebug    # Debug build
./gradlew assembleRelease  # Release build

# Quality checks
./gradlew detekt           # Static analysis
./gradlew test            # Unit tests
./gradlew connectedCheck   # Integration tests
```

### 📊 **Métricas del Proyecto**
- **📈 Cobertura de Tests**: 194 tests implementados
- **🏗️ Build System**: Gradle 9.0.0 (última versión)
- **🔧 Kotlin**: 2.0.21 con K2 Compiler
- **📱 Target SDK**: API 36 (Android 14+)

## 🎯 **Estado del Proyecto**

### ✅ **Logros Recientes**
- ✅ Migración exitosa a Gradle 9.0.0
- ✅ Implementación completa de Firebase Analytics
- ✅ 194 tests unitarios y de integración
- ✅ CI/CD optimizado con GitHub Actions
- ✅ Web scraping funcional desde API oficial

### 🎯 **Objetivos 2025**
- 🚀 Lanzamiento de versión beta
- 📱 Implementación de widgets
- � Sistema de notificaciones
- 🌍 Soporte multi-idioma
- 🏆 Modo playoff especial

---

**📱 Versión**: 1.0.0 (versionCode: 2)  
**📅 Última actualización**: Agosto 2025  
**🏗️ Build**: Gradle 9.0.0 + AGP 8.12.0  
**🔧 Kotlin**: 2.0.21 (K2 Compiler)
