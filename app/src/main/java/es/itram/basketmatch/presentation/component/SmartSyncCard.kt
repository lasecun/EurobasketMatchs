package es.itram.basketmatch.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.itram.basketmatch.R
import es.itram.basketmatch.data.sync.SmartSyncState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Componente para mostrar el estado de sincronización y permitir verificación manual
 */
@Composable
fun SmartSyncCard(
    syncState: SmartSyncState,
    lastSyncTime: LocalDateTime?,
    onManualSync: () -> Unit,
    onCheckUpdates: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sincronización Inteligente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                SyncStatusIcon(syncState = syncState)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Estado actual
            SyncStatusSection(
                syncState = syncState,
                lastSyncTime = lastSyncTime
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCheckUpdates,
                    enabled = !syncState.isActive,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verificar")
                }
                
                Button(
                    onClick = onManualSync,
                    enabled = !syncState.isActive,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sincronizar")
                }
            }
            
            // Información adicional
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoSection()
        }
    }
}

@Composable
private fun SyncStatusIcon(syncState: SmartSyncState) {
    when {
        syncState.isActive -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
        syncState.error != null -> {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }
        syncState.lastSyncSuccess -> {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Éxito",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        else -> {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Sincronizar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SyncStatusSection(
    syncState: SmartSyncState,
    lastSyncTime: LocalDateTime?
) {
    Column {
        // Estado actual
        Text(
            text = syncState.status,
            style = MaterialTheme.typography.bodyMedium,
            color = if (syncState.error != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        
        // Última sincronización
        lastSyncTime?.let { time ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Última sincronización: ${time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Error si existe
        syncState.error?.let { error ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Error: $error",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun InfoSection() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "ℹ️ Sincronización Inteligente",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "• Equipos y calendario: datos estáticos pre-cargados\n" +
                        "• Resultados y estadísticas: se actualizan manualmente\n" +
                        "• Menor consumo de batería y datos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Indicador compacto de estado de sincronización para otras pantallas
 */
@Composable
fun SmartSyncIndicator(
    syncState: SmartSyncState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when {
            syncState.isActive -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.dp
                )
                Text(
                    text = "Sincronizando...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            syncState.error != null -> {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Actualizado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
