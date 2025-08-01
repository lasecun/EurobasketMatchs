# EuroLeague 2026 - Aplicación Android

Una aplicación Android para seguir todos los partidos de la EuroLeague de baloncesto 2026.

## 📋 Descripción

Esta aplicación permite a los usuarios seguir el calendario completo de partidos de la EuroLeague 2026, incluyendo fechas, equipos, resultados y estadísticas. Desarrollada siguiendo principios de Clean Architecture y buenas prácticas de desarrollo Android.

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
- **Fuente Principal**: [EuroLeague Basketball Official API](https://www.euroleaguebasketball.net/)
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

### Frontend
- **UI Framework**: Jetpack Compose
- **Design System**: Material Design 3
- **Navigation**: Navigation Compose
- **Theming**: Dynamic Color (Material You)

### Arquitectura
- **Pattern**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt (Dagger)
- **State Management**: StateFlow, LiveData

### Datos
- **Local Database**: Room
- **Network**: Retrofit + OkHttp
- **Serialization**: Kotlinx Serialization / Gson
- **Image Loading**: Coil

### Testing
- **Unit Tests**: JUnit 5, MockK
- **UI Tests**: Espresso, Compose Testing
- **Integration Tests**: Room Testing, Retrofit Mock

### Build & CI/CD
- **Build System**: Gradle (Kotlin DSL)
- **Version Control**: Git
- **Static Analysis**: Detekt, Ktlint

## 📱 Características Principales

### Funcionalidades Core
1. **Calendario de Partidos**
   - Vista mensual y semanal
   - Filtros por equipos y fechas
   - Notificaciones para partidos favoritos

2. **Información de Equipos**
   - Listado de equipos participantes
   - Plantillas y estadísticas
   - Historial de enfrentamientos

3. **Resultados en Vivo**
   - Marcadores en tiempo real
   - Estadísticas del partido
   - Timeline de eventos

4. **Clasificaciones**
   - Tabla de posiciones
   - Estadísticas generales
   - Evolución temporal

### Funcionalidades Adicionales
- **Modo Offline**: Caché local para funcionamiento sin conexión
- **Favoritos**: Marcar equipos y partidos favoritos
- **Compartir**: Compartir resultados y estadísticas
- **Tema Oscuro**: Soporte completo para modo oscuro
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

### Requisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Kotlin 1.9.0+
- Android SDK 34
- Gradle 8.0+

### Setup Inicial
```bash
# Clonar el repositorio
git clone [repository-url]

# Abrir en Android Studio
# Sync del proyecto
# Ejecutar tests
./gradlew test

# Compilar la app
./gradlew assembleDebug
```

## � Documentación

### 📖 Documentación Técnica
Toda la documentación técnica del proyecto está organizada en la carpeta [`docs/`](./docs/):

- **🚀 Implementación**: [`docs/implementation/`](./docs/implementation/) - Planes de implementación y optimización
- **🧪 Testing**: [`docs/testing/`](./docs/testing/) - Guías y estado de las pruebas  
- **🔄 CI/CD**: [`docs/ci-cd/`](./docs/ci-cd/) - Documentación de pipelines y optimizaciones
- **🛡️ GitHub**: [`docs/github/`](./docs/github/) - Configuración y reglas de GitHub

### 📋 Índice de Documentación
Consulta el [índice completo de documentación](./docs/README.md) para una navegación detallada.

### ⚙️ Configuración
- **Workflows**: [`.github/workflows/`](./.github/workflows/) - Configuración de CI/CD
- **Detekt**: [`app/config/detekt/`](./app/config/detekt/) - Análisis estático de código
- **Gradle**: [`gradle/`](./gradle/) - Configuración de dependencias

## �👥 Equipo y Contribución

### Estructura del Equipo
- **Arquitecto de Software**: Diseño de arquitectura y patrones
- **Desarrollador Android**: Implementación de UI y lógica
- **Tester**: Testing y QA

### Proceso de Desarrollo
1. **Branching Strategy**: GitFlow
2. **Code Review**: Obligatorio para PRs
3. **Testing**: Cobertura mínima del 80%
4. **Documentation**: Código auto-documentado + README

## 📄 Licencia

[Especificar licencia del proyecto]

---

**Versión**: 1.0.0  
**Última actualización**: Julio 2025
# Test protection
