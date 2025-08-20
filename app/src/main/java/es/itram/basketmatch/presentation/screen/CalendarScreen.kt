package es.itram.basketmatch.presentation.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.itram.basketmatch.presentation.component.ErrorMessage
import es.itram.basketmatch.presentation.component.LoadingIndicator
import es.itram.basketmatch.presentation.component.MatchCard
import es.itram.basketmatch.presentation.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla de calendario que muestra los partidos por mes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onNavigateToMatchDetail: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // 📊 Analytics: Track screen view
    LaunchedEffect(Unit) {
        viewModel.trackScreenView()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Calendario") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        )

        if (isLoading) {
            LoadingIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            val currentError = error
            if (currentError != null) {
                ErrorMessage(
                    message = currentError,
                    onRetry = { viewModel.clearError() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header del mes
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.goToPreviousMonth() }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
                            }

                            Text(
                                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es"))),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(onClick = { viewModel.goToNextMonth() }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calendario
                    CalendarGrid(
                        yearMonth = currentMonth,
                        selectedDate = selectedDate,
                        onDateSelected = { date ->
                            Log.d("CalendarScreen", "📅 Fecha seleccionada en calendario: $date")
                            viewModel.selectDate(date)
                            // Cuando se selecciona una fecha, solo llamar al callback
                            // (el callback ya maneja la navegación de vuelta)
                            Log.d("CalendarScreen", "📅 Llamando onDateSelected callback con: $date")
                            onDateSelected(date)
                            Log.d("CalendarScreen", "📅 ✅ Callback ejecutado - navegación manejada por NavigationRoutes")
                        },
                        hasMatchesOnDate = { viewModel.hasMatchesOnDate(it) },
                        hasFavoriteTeamMatchesOnDate = { viewModel.hasFavoriteTeamMatchesOnDate(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Partidos del día seleccionado
                    val currentSelectedDate = selectedDate
                    if (currentSelectedDate != null) {
                        val dayMatches = viewModel.getMatchesForDate(currentSelectedDate)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentSelectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("es"))),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    TextButton(onClick = { viewModel.clearSelectedDate() }) {
                                        Text("Cerrar")
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (dayMatches.isEmpty()) {
                                    Text(
                                        text = "No hay partidos este día",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.heightIn(max = 300.dp)
                                    ) {
                                        items(dayMatches) { match ->
                                            val isHomeTeamFavorite = viewModel.isTeamFavorite(match.homeTeamName)
                                            val isAwayTeamFavorite = viewModel.isTeamFavorite(match.awayTeamName)
                                            
                                            // Debug log
                                            android.util.Log.d("CalendarScreen", "=== CalendarScreen Debug ===")
                                            android.util.Log.d("CalendarScreen", "homeTeam: ${match.homeTeamName}, checking favorite...")
                                            android.util.Log.d("CalendarScreen", "awayTeam: ${match.awayTeamName}, checking favorite...")
                                            android.util.Log.d("CalendarScreen", "============================")
                                            
                                            MatchCard(
                                                match = match,
                                                isHomeTeamFavorite = isHomeTeamFavorite,
                                                isAwayTeamFavorite = isAwayTeamFavorite,
                                                onMatchClick = onNavigateToMatchDetail
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    hasMatchesOnDate: (LocalDate) -> Boolean,
    hasFavoriteTeamMatchesOnDate: (LocalDate) -> Boolean
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Lunes = 0, Domingo = 6
    
    val today = LocalDate.now()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cabeceras de días de la semana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("L", "M", "X", "J", "V", "S", "D").forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Grilla de días
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Espacios en blanco antes del primer día
                items(firstDayOfWeek) {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }
                
                // Días del mes
                items(daysInMonth) { day ->
                    val date = yearMonth.atDay(day + 1)
                    val isSelected = date == selectedDate
                    val isToday = date == today
                    val hasMatches = hasMatchesOnDate(date)
                    val hasFavoriteMatches = hasFavoriteTeamMatchesOnDate(date)
                    
                    DayItem(
                        day = day + 1,
                        isSelected = isSelected,
                        isToday = isToday,
                        hasMatches = hasMatches,
                        hasFavoriteMatches = hasFavoriteMatches,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayItem(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasMatches: Boolean,
    hasFavoriteMatches: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            if (hasMatches) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                hasFavoriteMatches -> androidx.compose.ui.graphics.Color(0xFFFFD700) // Dorado para favoritos
                                else -> MaterialTheme.colorScheme.primary
                            },
                            CircleShape
                        )
                )
            }
        }
    }
}
