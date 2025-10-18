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
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🏀 Ejemplo de integración API oficial EuroLeague E2026
 */

@HiltViewModel
class EuroLeagueExampleViewModel @Inject constructor(
    private val officialApiDataSource: EuroLeagueOfficialApiDataSource
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
                val result = officialApiDataSource.getAllTeams()

                if (result.isSuccess) {
                    val teams = result.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        teams = teams,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }
}

data class EuroLeagueUiState(
    val teams: List<TeamWebDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

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
