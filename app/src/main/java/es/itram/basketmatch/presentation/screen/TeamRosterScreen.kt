package es.itram.basketmatch.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.presentation.viewmodel.TeamRosterViewModel
import es.itram.basketmatch.presentation.viewmodel.TeamRosterUiState

/**
 * Pantalla de roster de un equipo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamRosterScreen(
    teamTla: String,
    teamName: String = "",
    onNavigateBack: () -> Unit,
    onPlayerClick: (Player) -> Unit = {},
    viewModel: TeamRosterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Cargar roster cuando se monta la pantalla, pero solo si no está ya cargado para este equipo
    LaunchedEffect(teamTla, uiState.teamRoster?.teamCode) {
        val isCurrentTeamLoaded = uiState.teamRoster?.teamCode == teamTla
        val shouldLoad = !isCurrentTeamLoaded && !uiState.isLoading && !uiState.isRefreshing
        
        if (shouldLoad) {
            viewModel.loadTeamRoster(teamTla)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.teamRoster?.teamName ?: teamName.ifEmpty { teamTla },
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshTeamRoster(teamTla) },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        TeamRosterContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onRetry = { viewModel.loadTeamRoster(teamTla) },
            onClearError = { viewModel.clearError() },
            onPlayerClick = onPlayerClick
        )
    }
}

@Composable
private fun TeamRosterContent(
    uiState: TeamRosterUiState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    onPlayerClick: (Player) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Contenido principal
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Mostrar progreso si está disponible
                        val progressText = uiState.loadingProgress?.progressText ?: "Cargando roster..."
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        // Mostrar barra de progreso si hay información específica
                        uiState.loadingProgress?.let { progress ->
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { 
                                    if (progress.total > 0) {
                                        progress.current.toFloat() / progress.total.toFloat()
                                    } else {
                                        0f // Progreso indeterminado cuando total es 0
                                    }
                                },
                                modifier = Modifier.width(200.dp),
                            )
                        }
                    }
                }
                
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error,
                        onRetry = onRetry,
                        onDismiss = onClearError
                    )
                }
                
                uiState.teamRoster != null -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        RosterList(
                            teamRoster = uiState.teamRoster,
                            modifier = Modifier.fillMaxSize(),
                            onPlayerClick = onPlayerClick
                        )
                        
                        // Overlay de progreso durante refresh
                        if (uiState.isRefreshing) {
                            RefreshProgressOverlay(
                                loadingProgress = uiState.loadingProgress,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }
                
                else -> {
                    Text(
                        text = "No hay datos disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RosterList(
    teamRoster: TeamRoster,
    modifier: Modifier = Modifier,
    onPlayerClick: (Player) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header con información del equipo
        item {
            TeamInfoHeader(teamRoster = teamRoster)
        }
        
        // Lista de jugadores
        items(teamRoster.players) { player ->
            PlayerCard(
                player = player,
                onClick = { onPlayerClick(player) }
            )
        }
        
        // Spacer al final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TeamInfoHeader(teamRoster: TeamRoster) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo del equipo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (teamRoster.logoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(teamRoster.logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Logo de ${teamRoster.teamName}",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Mostrar icono por defecto si no hay logo
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = teamRoster.teamName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Temporada ${teamRoster.season}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${teamRoster.players.size} jugadores",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PlayerCard(
    player: Player,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (player.isCaptain) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto del jugador o icono por defecto
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (player.profileImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(player.profileImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de ${player.fullName}",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del jugador
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = player.fullName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (player.isCaptain) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "C",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    player.position?.let { position ->
                        Text(
                            text = position.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Combinar altura y peso en una sola línea
                    val physicalInfo = buildList {
                        player.height?.let { add(it) }
                        player.weight?.let { add(it) }
                    }.joinToString(" • ")
                    
                    if (physicalInfo.isNotEmpty()) {
                        val separator = if (player.position != null) " • " else ""
                        Text(
                            text = "$separator$physicalInfo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                player.nationality?.let { nationality ->
                    Text(
                        text = nationality,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Número de camiseta
            player.jersey?.let { jersey ->
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = jersey.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Cerrar")
            }
            
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

/**
 * Overlay que muestra el progreso durante la actualización
 */
@Composable
private fun RefreshProgressOverlay(
    loadingProgress: es.itram.basketmatch.presentation.viewmodel.LoadingProgress?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val progressText = loadingProgress?.progressText ?: "Actualizando roster..."
            Text(
                text = progressText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            
            loadingProgress?.let { progress ->
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 
                        if (progress.total > 0) {
                            progress.current.toFloat() / progress.total.toFloat()
                        } else {
                            0f // Progreso indeterminado cuando total es 0
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
