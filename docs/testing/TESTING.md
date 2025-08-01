# Testing Guide - EuroLeague Basketball App

Esta guía explica cómo ejecutar y mantener los tests unitarios e instrumentados de la aplicación.

## 📋 Configuración de Testing

### Dependencias incluidas:
- **JUnit 4**: Framework de testing básico
- **MockK**: Librería de mocking para Kotlin
- **Truth**: Assertions más legibles de Google
- **Turbine**: Testing de Kotlin Flow
- **Coroutines Test**: Testing de corrutinas
- **Arch Core Testing**: LiveData y ViewModel testing
- **Room Testing**: Testing de base de datos
- **Hilt Testing**: Inyección de dependencias en tests

## 🚀 Ejecutar Tests

### Tests Unitarios (Unit Tests)
```bash
# Todos los tests unitarios
./gradlew testDebugUnitTest

# Tests específicos
./gradlew testDebugUnitTest --tests "*.MainViewModelTest"
./gradlew testDebugUnitTest --tests "*.TeamMapperTest"
```

### Tests Instrumentados (Instrumented Tests)
```bash
# Todos los tests instrumentados
./gradlew connectedDebugAndroidTest

# Tests específicos
./gradlew connectedDebugAndroidTest --tests "*.TeamDaoTest"
```

### Tests con Cobertura
```bash
# Generar reporte de cobertura
./gradlew testDebugUnitTestCoverage
```

## 📁 Estructura de Tests

```
app/src/
├── test/                          # Tests Unitarios
│   └── java/es/itram/basketmatch/
│       ├── data/
│       │   ├── mapper/            # Tests de mappers
│       │   └── repository/        # Tests de repositorios
│       ├── domain/
│       │   └── usecase/           # Tests de casos de uso
│       ├── presentation/
│       │   └── viewmodel/         # Tests de ViewModels
│       └── testutil/              # Utilidades de testing
└── androidTest/                   # Tests Instrumentados
    └── java/es/itram/basketmatch/
        └── data/
            └── datasource/
                └── local/         # Tests de DAOs y Database
```

## 🧪 Tipos de Tests Incluidos

### 1. Tests de ViewModels
- **MainViewModelTest**: Prueba la lógica de la pantalla principal
- Cobertura:
  - ✅ Carga inicial de datos
  - ✅ Manejo de errores
  - ✅ Navegación de fechas
  - ✅ Filtrado de partidos
  - ✅ Estados de carga

### 2. Tests de Casos de Uso
- **GetAllTeamsUseCaseTest**: Prueba la obtención de equipos
- Cobertura:
  - ✅ Flujo normal de datos
  - ✅ Casos sin datos
  - ✅ Manejo de excepciones

### 3. Tests de Repositorios
- **TeamRepositoryImplTest**: Prueba la implementación del repositorio
- Cobertura:
  - ✅ Mapeo de entidades a dominio
  - ✅ Operaciones CRUD
  - ✅ Filtros y consultas

### 4. Tests de Mappers
- **TeamMapperTest**: Prueba las conversiones de datos
- Cobertura:
  - ✅ Mapeo de entidad a dominio
  - ✅ Mapeo de dominio a entidad
  - ✅ Mapeo bidireccional
  - ✅ Manejo de valores nulos/vacíos

### 5. Tests de Base de Datos
- **TeamDaoTest**: Prueba las operaciones de base de datos
- Cobertura:
  - ✅ Inserción y consulta
  - ✅ Actualización de datos
  - ✅ Filtros específicos
  - ✅ Operaciones de eliminación

## 🛠️ Utilidades de Testing

### TestDataFactory
Crea datos de prueba consistentes:
```kotlin
val testTeam = TestDataFactory.createTestTeam(
    id = "1",
    name = "Real Madrid"
)

val testTeams = TestDataFactory.createTeamsList(count = 5)
```

### MainDispatcherRule
Configura automáticamente el dispatcher para tests:
```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

### HiltTestRunner
Configuración personalizada para tests con Hilt:
```xml
<!-- En build.gradle.kts -->
testInstrumentationRunner = "es.itram.basketmatch.HiltTestRunner"
```

## 📊 Mejores Prácticas

### 1. Estructura AAA (Arrange-Act-Assert)
```kotlin
@Test
fun `when valid data is provided, then returns success`() = runTest {
    // Arrange (Given)
    val testData = TestDataFactory.createTestTeam()
    coEvery { repository.getTeam() } returns flowOf(testData)
    
    // Act (When)
    val result = useCase()
    
    // Assert (Then)
    result.test {
        val team = awaitItem()
        assertThat(team.name).isEqualTo("Real Madrid")
    }
}
```

### 2. Testing de Flow con Turbine
```kotlin
viewModel.teams.test {
    val teams = awaitItem()
    assertThat(teams).hasSize(2)
    awaitComplete()
}
```

### 3. Mocking con MockK
```kotlin
// Configuración de mock
coEvery { teamRepository.getAllTeams() } returns flowOf(testTeams)

// Verificación de llamadas
coVerify { teamRepository.getAllTeams() }
```

### 4. Tests de Database en Memoria
```kotlin
@Before
fun setup() {
    database = Room.inMemoryDatabaseBuilder(
        context,
        EuroLeagueDatabase::class.java
    ).build()
}
```

## 🎯 Cobertura de Tests

Los tests cubren:
- ✅ **Presentation Layer**: ViewModels y lógica de UI
- ✅ **Domain Layer**: Casos de uso y entidades
- ✅ **Data Layer**: Repositorios, DAOs y mappers
- ✅ **Integration**: Tests de base de datos
- ✅ **Error Handling**: Manejo de errores y casos edge

## 🔧 Configuración Adicional

### Agregar nuevos tests:
1. Crear archivo en la carpeta correspondiente
2. Usar las utilidades existentes (TestDataFactory, MainDispatcherRule)
3. Seguir la estructura AAA
4. Incluir casos positivos y negativos

### Para tests de UI (Compose):
```kotlin
@Test
fun myTest() {
    composeTestRule.setContent {
        // Tu composable aquí
    }
    
    composeTestRule
        .onNodeWithText("Texto")
        .assertIsDisplayed()
}
```

## 📈 Métricas

Los tests están diseñados para mantener:
- **Alta cobertura** (>80%) de código crítico
- **Tests rápidos** (<100ms por test unitario)
- **Tests estables** sin flakiness
- **Fácil mantenimiento** con utilidades compartidas

¡Los tests son fundamentales para mantener la calidad del código y facilitar el desarrollo futuro!
