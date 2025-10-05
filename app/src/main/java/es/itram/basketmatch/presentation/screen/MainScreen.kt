package es.itram.basketmatch.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import es.itram.basketmatch.R
import es.itram.basketmatch.presentation.component.ErrorMessage
import es.itram.basketmatch.presentation.component.HeaderDateSelector
import es.itram.basketmatch.presentation.component.EnhancedLoadingIndicator
import es.itram.basketmatch.presentation.component.EnhancedMatchCard
import es.itram.basketmatch.presentation.component.NoMatchesTodayCard
import es.itram.basketmatch.presentation.component.AppTopBar
import es.itram.basketmatch.presentation.component.SyncProgressIndicator
import es.itram.basketmatch.presentation.viewmodel.MainViewModel

/**
 * Pantalla principal con la lista de partidos filtrada por fecha
 * Versión mejorada con animaciones y mejor diseño visual
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToCalendar: () -> Unit,
    onNavigateToMatchDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // Obtener el padding de las barras de navegación del sistema
    val density = LocalDensity.current
    val navigationBarHeight = with(density) {
        WindowInsets.navigationBars.getBottom(density).toDp()
    }

    // 📊 Analytics: Track screen view
    LaunchedEffect(Unit) {
        viewModel.trackMainScreenView()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top Bar con settings
            AppTopBar(
                title = stringResource(R.string.app_name),
                onSettingsClick = onNavigateToSettings
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
            // Header con navegación de fechas - Con animación suave
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                HeaderDateSelector(
                    selectedDate = selectedDate,
                    onDateClick = {
                        // 📊 Analytics: Track calendar navigation from main screen
                        viewModel.trackCalendarNavigation()
                        onNavigateToCalendar()
                    },
                    onPreviousDay = { viewModel.goToPreviousDay() },
                    onNextDay = { viewModel.goToNextDay() }
                )
            }

            // Contenido principal con animaciones mejoradas
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = fadeOut()
            ) {
                when {


                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EnhancedLoadingIndicator(
                                message = "Cargando partidos de EuroLeague...",
                                showMessage = true
                            )
                        }
                    }

                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorMessage(
                                message = error ?: stringResource(R.string.error_unknown),
                                onRetry = { /* viewModel.retryLoading() */ }
                            )
                        }
                    }

                    matches.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            NoMatchesTodayCard(
                                isToday = viewModel.isSelectedDateToday(),
                                nextMatchDay = viewModel.findNextMatchDay(),
                                onNavigateToNextMatchDay = { viewModel.goToNextAvailableMatchDay() },
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = navigationBarHeight + 16.dp // Padding dinámico para barras de navegación
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(matches) { index, match ->
                                // Animación escalonada para cada card
                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        initialOffsetY = { it * (index + 1) }
                                    ) + fadeIn(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                ) {
                                    EnhancedMatchCard(
                                        match = match,
                                        onMatchClick = { matchId ->
                                            // 📊 Analytics: Track match selection from main screen
                                            viewModel.trackMatchClicked(match)
                                            onNavigateToMatchDetail(matchId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            } // Cierre del Column interno
        } // Cierre del Column principal
    }
}
