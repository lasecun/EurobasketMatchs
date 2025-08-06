package es.itram.basketmatch.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import es.itram.basketmatch.R
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.presentation.viewmodel.PlayerDetailViewModel

/**
 * Pantalla de detalle de un jugador
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    player: Player,
    teamName: String,
    onNavigateBack: () -> Unit,
    viewModel: PlayerDetailViewModel = hiltViewModel()
) {
    // 📊 Analytics: Track player view
    LaunchedEffect(player.code) {
        viewModel.trackScreenView()
        viewModel.trackPlayerViewed(player)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = player.fullName,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        PlayerDetailContent(
            player = player,
            teamName = teamName,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun PlayerDetailContent(
    player: Player,
    teamName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con foto y datos básicos
        PlayerHeaderCard(player = player, teamName = teamName)
        
        // Información personal
        PersonalInfoCard(player = player)
        
        // Información física
        PhysicalInfoCard(player = player)
        
        // Información deportiva
        SportsInfoCard(player = player)
    }
}

@Composable
private fun PlayerHeaderCard(
    player: Player,
    teamName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (player.isCaptain) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto del jugador
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp) // Altura un poco menor para mejor proporción
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (player.profileImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(player.profileImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.player_photo_description, player.fullName),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter // Enfocar en la parte superior para mostrar la cara
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Nombre del jugador
            Text(
                text = player.fullName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Número de camiseta y equipo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                player.jersey?.let { jersey ->
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = jersey.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = teamName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    player.position?.let { position ->
                        Text(
                            text = position.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Indicador de capitán
            if (player.isCaptain) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.captain),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalInfoCard(player: Player) {
    InfoCard(
        title = stringResource(R.string.personal_info),
        content = {
            InfoRow(
                label = stringResource(R.string.full_name),
                value = player.fullName
            )
            
            player.dateOfBirth?.let { 
                InfoRow(
                    label = stringResource(R.string.birth_date),
                    value = it
                )
            }
            
            player.placeOfBirth?.let { 
                InfoRow(
                    label = stringResource(R.string.birth_place),
                    value = it
                )
            }
            
            player.nationality?.let { 
                InfoRow(
                    label = stringResource(R.string.nationality),
                    value = it
                )
            }
        }
    )
}

@Composable
private fun PhysicalInfoCard(player: Player) {
    val hasPhysicalInfo = player.height != null || player.weight != null
    
    if (hasPhysicalInfo) {
        InfoCard(
            title = stringResource(R.string.physical_info),
            content = {
                player.height?.let { 
                    InfoRow(
                        label = stringResource(R.string.height),
                        value = it
                    )
                }
                
                player.weight?.let { 
                    InfoRow(
                        label = stringResource(R.string.weight),
                        value = it
                    )
                }
            }
        )
    }
}

@Composable
private fun SportsInfoCard(player: Player) {
    InfoCard(
        title = stringResource(R.string.sport_info),
        content = {
            player.position?.let { position ->
                InfoRow(
                    label = stringResource(R.string.position),
                    value = position.displayName
                )
            }
            
            player.jersey?.let { 
                InfoRow(
                    label = stringResource(R.string.jersey_number),
                    value = it.toString()
                )
            }
            
            player.experience?.let { 
                InfoRow(
                    label = stringResource(R.string.experience),
                    value = stringResource(R.string.experience_years, it)
                )
            }
            
            InfoRow(
                label = stringResource(R.string.status),
                value = if (player.isActive) stringResource(R.string.status_active) else stringResource(R.string.status_inactive)
            )
            
            if (player.isCaptain) {
                InfoRow(
                    label = stringResource(R.string.captain),
                    value = stringResource(R.string.yes)
                )
            }
        }
    )
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
