package es.itram.basketmatch.analytics.examples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.analytics.events.*
import es.itram.basketmatch.analytics.tracking.EventDispatcher
import es.itram.basketmatch.analytics.tracking.ScreenTracker
import es.itram.basketmatch.domain.model.LiveMatch
import es.itram.basketmatch.domain.model.MatchEvent
import kotlinx.coroutines.delay

/**
 * 🏀 Live Match Screen with Real-Time Analytics
 * 
 * Pantalla de partido en vivo con analytics avanzados que incluye:
 * - 📡 Real-time engagement tracking
 * - ⏱️ Session duration and attention metrics
 * - 🎯 Live event interaction tracking
 * - 📊 Performance monitoring for live updates
 * - 🔔 Push notification engagement
 * - 💫 User behavior pattern analysis
 * 
 * Optimizado para maximizar engagement y retención en contenido en vivo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMatchScreen(
    matchCode: String,
    onBackClick: () -> Unit,
    viewModel: LiveMatchViewModel = hiltViewModel(),
    eventDispatcher: EventDispatcher = hiltViewModel(),
    screenTracker: ScreenTracker = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var sessionStartTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // 📱 Screen tracking with live match context
    screenTracker.TrackScreen(
        screenName = AnalyticsManager.SCREEN_LIVE_MATCH,
        screenClass = "LiveMatchScreen",
        lifecycleOwner = lifecycleOwner
    ) {
        
        // 🎯 Track live match access
        LaunchedEffect(matchCode) {
            sessionStartTime = System.currentTimeMillis()
            
            eventDispatcher.dispatch(
                AnalyticsEvent.MatchContentEvent(
                    action = MatchAction.LIVE_MATCH_ENTERED,
                    matchCode = matchCode,
                    homeTeam = uiState.match?.homeTeam?.code,
                    awayTeam = uiState.match?.awayTeam?.code,
                    quarter = uiState.match?.currentQuarter,
                    timeRemaining = uiState.match?.timeRemaining,
                    source = "navigation",
                    engagement = mapOf(
                        "entry_method" to "direct_navigation",
                        "session_id" to sessionStartTime.toString(),
                        "match_status" to (uiState.match?.status ?: "unknown")
                    )
                )
            )
            
            viewModel.connectToLiveMatch(matchCode)
        }
        
        // ⏱️ Track session duration and engagement
        LaunchedEffect(Unit) {
            while (true) {
                delay(30000) // Every 30 seconds
                val currentTime = System.currentTimeMillis()
                val sessionDuration = (currentTime - sessionStartTime) / 1000
                val timeSinceLastInteraction = (currentTime - lastInteractionTime) / 1000
                
                // Track engagement metrics
                eventDispatcher.dispatch(
                    AnalyticsEvent.EngagementEvent(
                        action = "session_heartbeat",
                        sessionDuration = sessionDuration,
                        screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                        engagement = mapOf(
                            "time_since_last_interaction" to timeSinceLastInteraction.toString(),
                            "match_code" to matchCode,
                            "is_active" to (timeSinceLastInteraction < 60).toString()
                        )
                    )
                )
                
                // Track potential user disengagement
                if (timeSinceLastInteraction > 120) { // 2 minutes without interaction
                    eventDispatcher.dispatch(
                        AnalyticsEvent.UserInteractionEvent(
                            action = "user_potentially_disengaged",
                            element = "session_tracking",
                            screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                            value = "${timeSinceLastInteraction}s_inactive"
                        )
                    )
                }
            }
        }
        
        // 📊 Track live updates and performance
        LaunchedEffect(uiState.lastUpdate) {
            if (uiState.lastUpdate != null) {
                eventDispatcher.dispatch(
                    AnalyticsEvent.PerformanceEvent(
                        action = "live_update_received",
                        screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                        duration = uiState.updateLatency ?: 0,
                        context = mapOf(
                            "update_type" to (uiState.lastUpdate!!.type),
                            "match_code" to matchCode,
                            "connection_quality" to (uiState.connectionQuality ?: "unknown")
                        )
                    )
                )
            }
        }
        
        // 🔄 Track app lifecycle for live content
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        lastInteractionTime = System.currentTimeMillis()
                        eventDispatcher.dispatch(
                            AnalyticsEvent.MatchContentEvent(
                                action = MatchAction.LIVE_MATCH_RESUMED,
                                matchCode = matchCode,
                                homeTeam = uiState.match?.homeTeam?.code,
                                awayTeam = uiState.match?.awayTeam?.code,
                                quarter = uiState.match?.currentQuarter,
                                timeRemaining = uiState.match?.timeRemaining,
                                source = "lifecycle",
                                engagement = mapOf(
                                    "session_duration" to "${(System.currentTimeMillis() - sessionStartTime) / 1000}s"
                                )
                            )
                        )
                        viewModel.resumeLiveUpdates()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        val sessionDuration = (System.currentTimeMillis() - sessionStartTime) / 1000
                        eventDispatcher.dispatch(
                            AnalyticsEvent.MatchContentEvent(
                                action = MatchAction.LIVE_MATCH_PAUSED,
                                matchCode = matchCode,
                                homeTeam = uiState.match?.homeTeam?.code,
                                awayTeam = uiState.match?.awayTeam?.code,
                                quarter = uiState.match?.currentQuarter,
                                timeRemaining = uiState.match?.timeRemaining,
                                source = "lifecycle",
                                engagement = mapOf(
                                    "session_duration" to "${sessionDuration}s"
                                )
                            )
                        )
                        viewModel.pauseLiveUpdates()
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                
                // Track session end
                val finalSessionDuration = (System.currentTimeMillis() - sessionStartTime) / 1000
                eventDispatcher.dispatch(
                    AnalyticsEvent.EngagementEvent(
                        action = "live_session_ended",
                        sessionDuration = finalSessionDuration,
                        screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                        engagement = mapOf(
                            "match_code" to matchCode,
                            "reason" to "screen_disposed",
                            "final_score" to "${uiState.match?.homeScore ?: 0}-${uiState.match?.awayScore ?: 0}"
                        )
                    )
                )
            }
        }
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🔝 Live match header with status indicators
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.match != null) {
                                "${uiState.match!!.homeTeam.name} vs ${uiState.match!!.awayTeam.name}"
                            } else {
                                "Cargando partido..."
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        // Live indicator
                        if (uiState.isLive) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Text(
                                    text = "EN VIVO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            lastInteractionTime = System.currentTimeMillis()
                            val sessionDuration = (System.currentTimeMillis() - sessionStartTime) / 1000
                            
                            eventDispatcher.dispatch(
                                AnalyticsEvent.UserInteractionEvent(
                                    action = "back_pressed",
                                    element = "back_button",
                                    screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                                    value = "session_duration:${sessionDuration}s"
                                )
                            )
                            onBackClick()
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // 🔔 Notifications toggle
                    IconButton(
                        onClick = {
                            lastInteractionTime = System.currentTimeMillis()
                            eventDispatcher.dispatch(
                                AnalyticsEvent.UserInteractionEvent(
                                    action = "notifications_toggled",
                                    element = "notification_button",
                                    screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                                    value = if (uiState.notificationsEnabled) "disabled" else "enabled"
                                )
                            )
                            viewModel.toggleNotifications()
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.notificationsEnabled) {
                                androidx.compose.material.icons.Icons.Default.Notifications
                            } else {
                                androidx.compose.material.icons.Icons.Default.NotificationsOff
                            },
                            contentDescription = "Notificaciones"
                        )
                    }
                    
                    // 📤 Share button
                    IconButton(
                        onClick = {
                            lastInteractionTime = System.currentTimeMillis()
                            eventDispatcher.dispatch(
                                AnalyticsEvent.UserInteractionEvent(
                                    action = "share_live_match",
                                    element = "share_button",
                                    screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                                    value = "${uiState.match?.homeScore ?: 0}-${uiState.match?.awayScore ?: 0}"
                                )
                            )
                            viewModel.shareLiveMatch()
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Share,
                            contentDescription = "Compartir"
                        )
                    }
                }
            )
            
            when {
                uiState.isLoading -> {
                    LiveMatchLoadingContent(eventDispatcher)
                }
                
                uiState.error != null -> {
                    LiveMatchErrorContent(
                        error = uiState.error!!,
                        onRetry = {
                            lastInteractionTime = System.currentTimeMillis()
                            eventDispatcher.dispatch(
                                AnalyticsEvent.UserInteractionEvent(
                                    action = "retry_live_connection",
                                    element = "retry_button",
                                    screen = AnalyticsManager.SCREEN_LIVE_MATCH
                                )
                            )
                            viewModel.retryConnection(matchCode)
                        },
                        eventDispatcher = eventDispatcher
                    )
                }
                
                uiState.match != null -> {
                    LiveMatchContent(
                        match = uiState.match!!,
                        recentEvents = uiState.recentEvents,
                        onEventClick = { event ->
                            lastInteractionTime = System.currentTimeMillis()
                            viewModel.onEventInteraction(event)
                        },
                        onPlayerClick = { playerId ->
                            lastInteractionTime = System.currentTimeMillis()
                            viewModel.onPlayerInteraction(playerId, source = "live_stats")
                        },
                        eventDispatcher = eventDispatcher,
                        onInteraction = {
                            lastInteractionTime = System.currentTimeMillis()
                        }
                    )
                }
            }
        }
    }
}

/**
 * 📊 Live match main content
 */
@Composable
private fun LiveMatchContent(
    match: LiveMatch,
    recentEvents: List<MatchEvent>,
    onEventClick: (MatchEvent) -> Unit,
    onPlayerClick: (String) -> Unit,
    eventDispatcher: EventDispatcher,
    onInteraction: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // 🏀 Score card with real-time updates
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Score display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home team
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = match.homeTeam.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = match.homeScore.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // VS separator
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Away team
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = match.awayTeam.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = match.awayScore.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Game status
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Q${match.currentQuarter}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = match.timeRemaining ?: "00:00",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
            
            // Track score views
            LaunchedEffect(match.homeScore, match.awayScore) {
                onInteraction()
                eventDispatcher.dispatch(
                    AnalyticsEvent.MatchContentEvent(
                        action = MatchAction.SCORE_VIEWED,
                        matchCode = match.code,
                        homeTeam = match.homeTeam.code,
                        awayTeam = match.awayTeam.code,
                        quarter = match.currentQuarter,
                        timeRemaining = match.timeRemaining,
                        source = "live_update",
                        engagement = mapOf(
                            "home_score" to match.homeScore.toString(),
                            "away_score" to match.awayScore.toString(),
                            "score_diff" to "${kotlin.math.abs(match.homeScore - match.awayScore)}"
                        )
                    )
                )
            }
        }
        
        // 📈 Quick stats section
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Estadísticas del Partido",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Add quick stats here with analytics tracking
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickStatItem(
                            label = "FG%",
                            homeValue = "${match.homeTeamStats?.fieldGoalPercentage ?: 0}%",
                            awayValue = "${match.awayTeamStats?.fieldGoalPercentage ?: 0}%",
                            onView = {
                                onInteraction()
                                eventDispatcher.dispatch(
                                    AnalyticsEvent.UserInteractionEvent(
                                        action = "stat_viewed",
                                        element = "field_goal_percentage",
                                        screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                                        value = "fg_percentage"
                                    )
                                )
                            }
                        )
                        
                        QuickStatItem(
                            label = "REB",
                            homeValue = "${match.homeTeamStats?.rebounds ?: 0}",
                            awayValue = "${match.awayTeamStats?.rebounds ?: 0}",
                            onView = {
                                onInteraction()
                                eventDispatcher.dispatch(
                                    AnalyticsEvent.UserInteractionEvent(
                                        action = "stat_viewed",
                                        element = "rebounds",
                                        screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                                        value = "rebounds"
                                    )
                                )
                            }
                        )
                        
                        QuickStatItem(
                            label = "AST",
                            homeValue = "${match.homeTeamStats?.assists ?: 0}",
                            awayValue = "${match.awayTeamStats?.assists ?: 0}",
                            onView = {
                                onInteraction()
                                eventDispatcher.dispatch(
                                    AnalyticsEvent.UserInteractionEvent(
                                        action = "stat_viewed",
                                        element = "assists",
                                        screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                                        value = "assists"
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
        
        // 📰 Recent events with engagement tracking
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Últimas Jugadas",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
        
        items(
            items = recentEvents.take(10), // Show last 10 events
            key = { event -> event.id }
        ) { event ->
            LiveEventCard(
                event = event,
                onClick = {
                    onInteraction()
                    eventDispatcher.dispatch(
                        AnalyticsEvent.UserInteractionEvent(
                            action = "live_event_tapped",
                            element = "event_card",
                            screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                            value = "${event.type}:${event.description}"
                        )
                    )
                    onEventClick(event)
                },
                eventDispatcher = eventDispatcher
            )
        }
    }
}

/**
 * 📊 Quick stat item with analytics
 */
@Composable
private fun QuickStatItem(
    label: String,
    homeValue: String,
    awayValue: String,
    onView: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = homeValue,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = awayValue,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
    
    LaunchedEffect(Unit) {
        onView()
    }
}

/**
 * 🎯 Live event card with micro-analytics
 */
@Composable
private fun LiveEventCard(
    event: MatchEvent,
    onClick: () -> Unit,
    eventDispatcher: EventDispatcher
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event time
            Text(
                text = event.gameTime ?: "00:00",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(48.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Event description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (event.playerName != null) {
                    Text(
                        text = event.playerName!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Event type indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (event.type) {
                        "FIELD_GOAL" -> MaterialTheme.colorScheme.primary
                        "FREE_THROW" -> MaterialTheme.colorScheme.secondary
                        "FOUL" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier.size(8.dp)
            ) {}
        }
    }
}

/**
 * ⏳ Loading content for live match
 */
@Composable
private fun LiveMatchLoadingContent(
    eventDispatcher: EventDispatcher
) {
    LaunchedEffect(Unit) {
        eventDispatcher.dispatch(
            AnalyticsEvent.UserInteractionEvent(
                action = "live_loading_displayed",
                element = "loading_indicator",
                screen = AnalyticsManager.SCREEN_LIVE_MATCH
            )
        )
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Conectando al partido en vivo...")
            Text(
                text = "Esto puede tardar unos segundos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * ❌ Error content for live match
 */
@Composable
private fun LiveMatchErrorContent(
    error: String,
    onRetry: () -> Unit,
    eventDispatcher: EventDispatcher
) {
    LaunchedEffect(error) {
        eventDispatcher.dispatch(
            AnalyticsEvent.UserInteractionEvent(
                action = "live_error_displayed",
                element = "error_message",
                screen = AnalyticsManager.SCREEN_LIVE_MATCH,
                value = error
            )
        )
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Error en conexión en vivo",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onRetry) {
                    Text("Reintentar conexión")
                }
            }
        }
    }
}
