package es.itram.basketmatch.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.itram.basketmatch.R
import es.itram.basketmatch.data.sync.SmartSyncState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Componente mejorado para mostrar el estado de sincronización con mejor diseño visual
 */
@Composable
fun EnhancedSmartSyncCard(
    syncState: SmartSyncState,
    lastSyncTime: LocalDateTime?,
    onManualSync: () -> Unit,
    onCheckUpdates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = syncState.isActive
    val hasError = !syncState.error.isNullOrBlank()
    val isSuccess = syncState.lastSyncSuccess && !isActive && !hasError
    
    // Animación de color basada en el estado
    val cardBackgroundColor by animateColorAsState(
        targetValue = when {
            hasError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            isActive -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
            isSuccess -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = "cardBackground"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            hasError -> MaterialTheme.colorScheme.error
            isActive -> MaterialTheme.colorScheme.tertiary
            isSuccess -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = "borderColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = when {
                        hasError -> Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                        isActive -> Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                        isSuccess -> Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                        else -> Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    }
                )
                .padding(20.dp)
        ) {
            // Header mejorado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sincronización Inteligente",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = getStatusDescription(syncState),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Indicador de estado mejorado
                StatusIndicator(syncState = syncState, borderColor = borderColor)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mostrar error si existe
            if (!syncState.error.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = syncState.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Información de última sincronización
            if (lastSyncTime != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Última sincronización: ${lastSyncTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Botones de acción mejorados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasError || !isActive) {
                    Button(
                        onClick = onManualSync,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = borderColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isActive
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasError) "Reintentar" else "Sincronizar",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                OutlinedButton(
                    onClick = onCheckUpdates,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = borderColor
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(borderColor, borderColor))
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isActive
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verificar",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    syncState: SmartSyncState,
    borderColor: Color
) {
    val (icon, description) = when {
        !syncState.error.isNullOrBlank() -> Icons.Default.Warning to "Error"
        syncState.isActive -> Icons.Default.Refresh to when {
            syncState.isInitializing -> "Inicializando..."
            syncState.isSyncing -> "Sincronizando..."
            syncState.isCheckingUpdates -> "Verificando..."
            else -> "Procesando..."
        }
        syncState.lastSyncSuccess -> Icons.Default.CheckCircle to "Actualizado"
        else -> Icons.Default.Warning to "Pendiente"
    }
    
    Surface(
        color = borderColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = borderColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = borderColor
            )
        }
    }
}

private fun getStatusDescription(syncState: SmartSyncState): String {
    return when {
        !syncState.error.isNullOrBlank() -> "Error al sincronizar los datos"
        syncState.isInitializing -> "Inicializando sistema de datos..."
        syncState.isSyncing -> "Descargando la información más reciente..."
        syncState.isCheckingUpdates -> "Verificando actualizaciones disponibles..."
        syncState.lastSyncSuccess -> "Todos los datos están actualizados"
        else -> "Listo para sincronizar"
    }
}
