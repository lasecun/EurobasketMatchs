package es.itram.basketmatch.analytics.examples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.analytics.events.AnalyticsEvent
import es.itram.basketmatch.analytics.events.TeamAction
import es.itram.basketmatch.analytics.tracking.EventDispatcher
import es.itram.basketmatch.analytics.tracking.ScreenTracker
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.TeamRoster

/**
 * 🏀 Enhanced Team Roster Screen with Complete Analytics Integration
 * 
 * Ejemplo completo de pantalla con analytics que demuestra:
 * - 📱 Screen tracking automático
 * - 🎯 Event tracking para interacciones de usuario
 * - ⚡ Performance monitoring
 * - 🔍 User journey tracking
 * - 📊 Engagement metrics
 * 
 * Optimizado para SEO móvil y insights de comportamiento de usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTeamRosterScreen(
    teamCode: String,
    teamName: String,
    onPlayerClick: (Player) -> Unit,
    onBackClick: () -> Unit,
    viewModel: EnhancedTeamRosterViewModel = hiltViewModel(),
    eventDispatcher: EventDispatcher = hiltViewModel(),
    screenTracker: ScreenTracker = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // 📱 Automatic screen tracking with lifecycle awareness
    screenTracker.TrackScreen(
        screenName = AnalyticsManager.SCREEN_TEAM_ROSTER,
        screenClass = "EnhancedTeamRosterScreen",
        lifecycleOwner = lifecycleOwner
    ) {
        
        // 🎯 Track screen entry with context
        LaunchedEffect(teamCode) {
            // Track team content access
            eventDispatcher.dispatch(
                AnalyticsEvent.TeamContentEvent(
                    action = TeamAction.ROSTER_ACCESSED,
                    teamCode = teamCode,
                    teamName = teamName,
                    source = "navigation",
                    context = "team_detail"
                )
            )
            
            // Load roster data
            viewModel.loadTeamRoster(teamCode, source = "screen_entry")
        }
        
        // 📊 Track screen lifecycle events
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        // Track screen resume (user returning to screen)
                        eventDispatcher.dispatch(
                            AnalyticsEvent.UserInteractionEvent(
                                action = "screen_resumed",
                                element = "lifecycle",
                                screen = AnalyticsManager.SCREEN_TEAM_ROSTER
                            )
                        )
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        // Track screen pause (user leaving screen)
                        eventDispatcher.dispatch(
                            AnalyticsEvent.UserInteractionEvent(
                                action = "screen_paused",
                                element = "lifecycle",
                                screen = AnalyticsManager.SCREEN_TEAM_ROSTER
                            )
                        )
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🔝 Top App Bar with back navigation tracking
            TopAppBar(
                title = { 
                    Text(text = "$teamName Roster")
                },
                navigationIcon = {
                    IconButton(
                        onClick = { 
                            // Track back navigation
                            eventDispatcher.dispatch(
                                AnalyticsEvent.UserInteractionEvent(
                                    action = "back_pressed",
                                    element = "back_button",
                                    screen = AnalyticsManager.SCREEN_TEAM_ROSTER
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
                    // 💝 Favorite button with analytics
                    IconButton(
                        onClick = {
                            eventDispatcher.dispatch(
                                AnalyticsEvent.UserInteractionEvent(
                                    action = "favorite_tapped",
                                    element = "favorite_button",
                                    screen = AnalyticsManager.SCREEN_TEAM_ROSTER,
                                    value = teamCode
                                )
                            )
                            viewModel.addTeamToFavorites()
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Favorite,
                            contentDescription = "Añadir a favoritos"
                        )
                    }
                    
                    // 📤 Share button with analytics
                    IconButton(
                        onClick = {
                            eventDispatcher.dispatch(
                                AnalyticsEvent.UserInteractionEvent(
                                    action = "share_tapped",
                                    element = "share_button",
                                    screen = AnalyticsManager.SCREEN_TEAM_ROSTER,
                                    value = teamCode
                                )
                            )
                            viewModel.shareRoster("system_share")
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Share,
                            contentDescription = "Compartir roster"
                        )
                    }
                }
            )
            
            // 🔍 Search bar with real-time analytics
            var searchQuery by remember { mutableStateOf("") }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newQuery ->
                    searchQuery = newQuery
                    
                    // Track search as user types (debounced in real implementation)
                    if (newQuery.length >= 3) {
                        viewModel.searchPlayers(newQuery)
                    }
                },
                label = { Text("Buscar jugadores...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
            
            // 🎛️ Filter chips with analytics
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                val positions = listOf("Todos", "Guard", "Forward", "Center")
                items(positions) { position ->
                    FilterChip(
                        onClick = {
                            // Track filter usage
                            eventDispatcher.dispatch(
                                AnalyticsEvent.UserInteractionEvent(
                                    action = "filter_applied",
                                    element = "position_filter",
                                    screen = AnalyticsManager.SCREEN_TEAM_ROSTER,
                                    value = position
                                )
                            )
                            viewModel.filterByPosition(position)
                        },
                        label = { Text(position) },
                        selected = uiState.selectedFilter == position
                    )
                }
            }
            
            // 📋 Main content with analytics
            when {
                uiState.isLoading -> {
                    // Track loading state engagement
                    LaunchedEffect(Unit) {
                        eventDispatcher.dispatch(
                            AnalyticsEvent.UserInteractionEvent(
                                action = "loading_displayed",
                                element = "loading_indicator",
                                screen = AnalyticsManager.SCREEN_TEAM_ROSTER
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
                            Text("Cargando roster...")
                        }
                    }
                }
                
                uiState.error != null -> {
                    // Track error state engagement
                    LaunchedEffect(uiState.error) {
                        eventDispatcher.dispatch(
                            AnalyticsEvent.UserInteractionEvent(
                                action = "error_displayed",
                                element = "error_message",
                                screen = AnalyticsManager.SCREEN_TEAM_ROSTER,
                                value = uiState.error
                            )
                        )
                    }
                    
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { 
                            // Track retry action
                            eventDispatcher.dispatch(
                                AnalyticsEvent.UserInteractionEvent(
                                    action = "retry_tapped",
                                    element = "retry_button",
                                    screen = AnalyticsManager.SCREEN_TEAM_ROSTER
                                )
                            )
                            viewModel.refreshRoster(teamCode)
                        },
                        onDismiss = { 
                            viewModel.clearError()
                        }
                    )
                }
                
                uiState.teamRoster != null -> {
                    // 📊 Track successful data display
                    LaunchedEffect(uiState.teamRoster) {
                        eventDispatcher.dispatch(
                            AnalyticsEvent.UserInteractionEvent(
                                action = "roster_displayed",
                                element = "player_list",
                                screen = AnalyticsManager.SCREEN_TEAM_ROSTER,
                                value = "${uiState.teamRoster!!.players.size}_players"
                            )
                        )
                    }
                    
                    RosterContent(
                        roster = uiState.teamRoster!!,
                        searchQuery = searchQuery,
                        selectedFilter = uiState.selectedFilter,
                        onPlayerClick = { player ->
                            // Track player selection with detailed context
                            viewModel.onPlayerSelected(player, source = "roster_card")
                            onPlayerClick(player)
                        },
                        eventDispatcher = eventDispatcher
                    )
                }
            }
        }
    }
}

/**
 * 👥 Roster content with granular analytics
 */
@Composable
private fun RosterContent(
    roster: TeamRoster,
    searchQuery: String,
    selectedFilter: String?,
    onPlayerClick: (Player) -> Unit,
    eventDispatcher: EventDispatcher
) {
    // Filter players based on search and filters
    val filteredPlayers = remember(roster.players, searchQuery, selectedFilter) {
        roster.players.filter { player ->
            val matchesSearch = searchQuery.isBlank() || 
                player.fullName.contains(searchQuery, ignoreCase = true) ||
                player.position?.name?.contains(searchQuery, ignoreCase = true) == true
            
            val matchesFilter = selectedFilter == null || selectedFilter == "Todos" ||
                player.position?.name == selectedFilter
            
            matchesSearch && matchesFilter
        }.sortedBy { it.jersey ?: 999 }
    }
    
    // Track filtered results
    LaunchedEffect(filteredPlayers.size, searchQuery, selectedFilter) {
        if (searchQuery.isNotBlank() || selectedFilter != null) {
            eventDispatcher.dispatch(
                AnalyticsEvent.UserInteractionEvent(
                    action = "results_filtered",
                    element = "player_list",
                    screen = AnalyticsManager.SCREEN_TEAM_ROSTER,
                    value = "${filteredPlayers.size}_results"
                )
            )
        }
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Track list scroll engagement
        item {
            LaunchedEffect(Unit) {
                eventDispatcher.dispatch(
                    AnalyticsEvent.UserInteractionEvent(
                        action = "list_scrolled",
                        element = "player_list",
                        screen = AnalyticsManager.SCREEN_TEAM_ROSTER
                    )
                )
            }
        }
        
        items(
            items = filteredPlayers,
            key = { player -> player.code }
        ) { player ->
            PlayerCard(
                player = player,
                onClick = { 
                    // Track individual player card interaction
                    eventDispatcher.dispatch(
                        AnalyticsEvent.UserInteractionEvent(
                            action = "player_card_tapped",
                            element = "player_card",
                            screen = AnalyticsManager.SCREEN_TEAM_ROSTER,
                            value = "${player.code}:${player.fullName}"
                        )
                    )
                    onPlayerClick(player)
                },
                eventDispatcher = eventDispatcher
            )
        }
        
        // Track empty results
        if (filteredPlayers.isEmpty()) {
            item {
                LaunchedEffect(Unit) {
                    eventDispatcher.dispatch(
                        AnalyticsEvent.UserInteractionEvent(
                            action = "empty_results_displayed",
                            element = "empty_state",
                            screen = AnalyticsManager.SCREEN_TEAM_ROSTER,
                            value = "search:$searchQuery;filter:$selectedFilter"
                        )
                    )
                }
                
                EmptyResultsContent()
            }
        }
    }
}

/**
 * 👤 Player card with micro-analytics
 */
@Composable
private fun PlayerCard(
    player: Player,
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Jersey number
            Card(
                modifier = Modifier.size(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.jersey?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Player info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = player.fullName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = player.position?.name ?: "Posición no especificada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (player.height != null || player.weight != null) {
                    Text(
                        text = "${player.height ?: "?"} • ${player.weight ?: "?"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Track image load performance when image is displayed
            if (player.profileImageUrl != null) {
                LaunchedEffect(player.profileImageUrl) {
                    // This would be integrated with actual image loading
                    // trackImageLoadPerformance(player.profileImageUrl)
                }
            }
        }
    }
}

/**
 * 🚫 Empty results content
 */
@Composable
private fun EmptyResultsContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No se encontraron jugadores",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Intenta con otro término de búsqueda o filtro",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * ❌ Error content with retry analytics
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = "Error cargando roster",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                    Button(onClick = onRetry) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}
