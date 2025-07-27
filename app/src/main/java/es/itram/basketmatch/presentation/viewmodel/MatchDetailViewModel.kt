package es.itram.basketmatch.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.usecase.GetMatchByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de detalle del partido
 */
@HiltViewModel
class MatchDetailViewModel @Inject constructor(
    private val getMatchByIdUseCase: GetMatchByIdUseCase
) : ViewModel() {

    private val _match = MutableStateFlow<Match?>(null)
    val match: StateFlow<Match?> = _match.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    companion object {
        private const val TAG = "MatchDetailViewModel"
    }

    /**
     * Carga los detalles del partido por ID
     */
    fun loadMatchDetails(matchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                Log.d(TAG, "🏀 Cargando detalles del partido: $matchId")
                
                val matchFlow = getMatchByIdUseCase(matchId)
                val matchData = matchFlow.first()
                
                if (matchData != null) {
                    _match.value = matchData
                    Log.d(TAG, "✅ Partido cargado: ${matchData.homeTeamName} vs ${matchData.awayTeamName}")
                } else {
                    _error.value = "Partido no encontrado"
                    Log.w(TAG, "⚠️ Partido no encontrado: $matchId")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando partido $matchId", e)
                _error.value = "Error al cargar el partido: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpia el error
     */
    fun clearError() {
        _error.value = null
    }
}
