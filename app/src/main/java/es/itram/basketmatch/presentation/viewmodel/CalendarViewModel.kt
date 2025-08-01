package es.itram.basketmatch.presentation.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.usecase.GetAllMatchesUseCase
import es.itram.basketmatch.domain.usecase.GetAllTeamsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * ViewModel para la pantalla de calendario
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getAllMatchesUseCase: GetAllMatchesUseCase,
    private val getAllTeamsUseCase: GetAllTeamsUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _teams = MutableStateFlow<Map<String, Team>>(emptyMap())
    val teams: StateFlow<Map<String, Team>> = _teams.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadData()
        
        // 📊 Analytics: Track calendar screen access
        trackScreenView()
    }
    
    /**
     * 📊 Analytics: Track screen view
     */
    fun trackScreenView() {
        analyticsManager.trackScreenView(
            screenName = AnalyticsManager.SCREEN_CALENDAR,
            screenClass = "CalendarViewModel"
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cargar equipos y partidos de manera simple
                val teams = getAllTeamsUseCase().first()
                val matches = getAllMatchesUseCase().first()
                
                _teams.value = teams.associateBy { it.id }
                _matches.value = matches
                _error.value = null
                
                // 📊 Analytics: Track successful calendar data load
                analyticsManager.logCustomEvent("calendar_data_loaded", Bundle().apply {
                    putInt("teams_count", teams.size)
                    putInt("matches_count", matches.size)
                    putString("current_month", _currentMonth.value.toString())
                })
                
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
                
                // 📊 Analytics: Track calendar data load error
                analyticsManager.logCustomEvent("calendar_load_error", Bundle().apply {
                    putString("error_message", e.message)
                    putString("error_class", e.javaClass.simpleName)
                })
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun goToPreviousMonth() {
        val previousMonth = _currentMonth.value.minusMonths(1)
        _currentMonth.value = previousMonth
        
        // 📊 Analytics: Track month navigation
        analyticsManager.logCustomEvent("calendar_month_navigation", Bundle().apply {
            putString("direction", "previous")
            putString("new_month", previousMonth.toString())
            putString("screen", "calendar")
        })
    }

    fun goToNextMonth() {
        val nextMonth = _currentMonth.value.plusMonths(1)
        _currentMonth.value = nextMonth
        
        // 📊 Analytics: Track month navigation
        analyticsManager.logCustomEvent("calendar_month_navigation", Bundle().apply {
            putString("direction", "next")
            putString("new_month", nextMonth.toString())
            putString("screen", "calendar")
        })
    }

    fun goToCurrentMonth() {
        val currentMonth = YearMonth.now()
        _currentMonth.value = currentMonth
        
        // 📊 Analytics: Track return to current month
        analyticsManager.logCustomEvent("calendar_month_navigation", Bundle().apply {
            putString("direction", "current")
            putString("new_month", currentMonth.toString())
            putString("screen", "calendar")
        })
    }

    fun selectDate(date: LocalDate) {
        val previousDate = _selectedDate.value
        _selectedDate.value = if (_selectedDate.value == date) null else date
        
        // 📊 Analytics: Track date selection
        analyticsManager.logCustomEvent("calendar_date_selected", Bundle().apply {
            putString("selected_date", date.toString())
            putBoolean("is_deselection", _selectedDate.value == null)
            putInt("matches_on_date", getMatchesForDate(date).size)
            putString("screen", "calendar")
            putString("month", _currentMonth.value.toString())
        })
    }

    fun getMatchesForDate(date: LocalDate): List<Match> {
        val matchesForDate = _matches.value.filter { match ->
            match.dateTime.toLocalDate() == date
        }.sortedBy { it.dateTime }
        
        // 📊 Analytics: Track date exploration (only for dates with matches)
        if (matchesForDate.isNotEmpty()) {
            analyticsManager.logCustomEvent("calendar_date_explored", Bundle().apply {
                putString("explored_date", date.toString())
                putInt("matches_found", matchesForDate.size)
                putString("screen", "calendar")
            })
        }
        
        return matchesForDate
    }

    fun hasMatchesOnDate(date: LocalDate): Boolean {
        return _matches.value.any { match ->
            match.dateTime.toLocalDate() == date
        }
    }

    fun getTeamById(teamId: String): Team? {
        return _teams.value[teamId]
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSelectedDate() {
        _selectedDate.value = null
    }
}
