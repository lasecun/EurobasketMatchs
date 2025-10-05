package es.itram.basketmatch.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🏀 Ejemplo de integración de la API oficial de EuroLeague en UI
 *
 * Este ejemplo muestra cómo usar la nueva arquitectura híbrida:
 * 1. API oficial de EuroLeague (principal)
 * 2. API de feeds (fallback)
 * 3. Datos hardcodeados (emergencia)
 */

// ViewModel que usa la nueva integración
@HiltViewModel
class EuroLeagueExampleViewModel @Inject constructor(
    private val euroLeagueRemoteDataSource: EuroLeagueRemoteDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(EuroLeagueUiState())
    val uiState: StateFlow<EuroLeagueUiState> = _uiState.asStateFlow()

    init {
        loadTeams()
    }

    fun loadTeams() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = euroLeagueRemoteDataSource.getAllTeams()

                if (result.isSuccess) {
                    val teams = result.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        teams = teams,
                        dataSource = determineDataSource(teams)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error cargando equipos: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    private fun determineDataSource(teams: List<TeamWebDto>): String {
        return when {
            teams.isEmpty() -> "Sin datos"
            teams.size >= 16 -> "API Oficial EuroLeague 🏆"
            teams.size >= 8 -> "API de Feeds (Fallback) 🔄"
            else -> "Datos de Emergencia 🆘"
        }
    }
}

// Estado de la UI
data class EuroLeagueUiState(
    val isLoading: Boolean = false,
    val teams: List<TeamWebDto> = emptyList(),
    val error: String? = null,
    val dataSource: String = "Cargando..."
)

// Composable de ejemplo
@Composable
fun EuroLeagueExampleScreen(
    viewModel: EuroLeagueExampleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con información de la fuente de datos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🏀 Integración API EuroLeague",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fuente de datos: ${uiState.dataSource}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Equipos cargados: ${uiState.teams.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de recarga
        Button(
            onClick = { viewModel.loadTeams() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔄 Recargar Datos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido principal
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando desde API oficial...")
                    }
                }
            }

            uiState.error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            uiState.teams.isNotEmpty() -> {
                LazyColumn {
                    items(uiState.teams) { team ->
                        TeamItem(team = team)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay equipos disponibles")
                }
            }
        }
    }
}

@Composable
fun TeamItem(team: TeamWebDto) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = team.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = team.fullName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (team.country != null) {
                Text(
                    text = "🌍 ${team.country}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (team.venue != null) {
                Text(
                    text = "🏟️ ${team.venue}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
