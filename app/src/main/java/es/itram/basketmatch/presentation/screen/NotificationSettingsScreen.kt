package es.itram.basketmatch.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import es.itram.basketmatch.BuildConfig
import es.itram.basketmatch.presentation.viewmodel.NotificationSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.notificationSettings.collectAsState()
    
    // Launcher para solicitar permisos de notificación
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido - las notificaciones ya están habilitadas en el ViewModel
            // No necesitamos hacer nada adicional aquí
        } else {
            // Permiso denegado - desactivar las notificaciones
            viewModel.setNotificationsEnabled(false)
        }
    }
    
    // Collector para el evento de solicitud de permisos
    LaunchedEffect(Unit) {
        viewModel.requestPermissionEvent.collect {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Sección de DEBUG para testing
            if (BuildConfig.DEBUG) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "🔧 MODO DEBUG", 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    // Solo mostrará logs en Debug
                                    viewModel.debugPrintFCMToken()
                                }
                            ) {
                                Text("Ver FCM Token")
                            }
                            
                            Button(
                                onClick = {
                                    // Solo mostrará logs en Debug
                                    viewModel.debugTestNotification()
                                }
                            ) {
                                Text("Test Notificación")
                            }
                        }
                        
                        Text(
                            "Revisa los logs de Android Studio para ver el token FCM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Configuración general
            NotificationSectionCard(
                title = "Configuración General",
                icon = Icons.Default.Notifications
            ) {
                NotificationSwitchSetting(
                    title = "Habilitar Notificaciones",
                    description = "Recibir todas las notificaciones de la app",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                    icon = Icons.Default.Notifications
                )
            }
            
            // Configuración de partidos
            if (settings.notificationsEnabled) {
                NotificationSectionCard(
                    title = "Partidos",
                    icon = Icons.Default.Notifications
                ) {
                    NotificationSwitchSetting(
                        title = "Recordatorios de Partidos",
                        description = "Notificaciones antes de que empiecen los partidos de tus equipos favoritos",
                        checked = settings.matchRemindersEnabled,
                        onCheckedChange = { viewModel.setMatchRemindersEnabled(it) },
                        icon = Icons.Default.DateRange
                    )
                    
                    if (settings.matchRemindersEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        ReminderTimeSetting(
                            currentTime = settings.reminderTimeMinutes,
                            onTimeChange = { viewModel.setReminderTime(it) }
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NotificationSwitchSetting(
                        title = "Resultados de Partidos",
                        description = "Notificaciones cuando terminen los partidos de tus equipos favoritos",
                        checked = settings.resultNotificationsEnabled,
                        onCheckedChange = { viewModel.setResultNotificationsEnabled(it) },
                        icon = Icons.Default.CheckCircle
                    )
                }
                
                // Configuración de equipos
                NotificationSectionCard(
                    title = "Equipos",
                    icon = Icons.Default.Person
                ) {
                    NotificationSwitchSetting(
                        title = "Noticias de Equipos",
                        description = "Recibir noticias y actualizaciones de tus equipos favoritos",
                        checked = settings.teamNewsEnabled,
                        onCheckedChange = { viewModel.setTeamNewsEnabled(it) },
                        icon = Icons.Default.Info
                    )
                }
                
                // Información adicional
                NotificationInfoCard()
            }
        }
    }
}

@Composable
private fun NotificationSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun NotificationSwitchSetting(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeSetting(
    currentTime: Int,
    onTimeChange: (Int) -> Unit
) {
    val timeOptions = listOf(15, 30, 60, 120) // minutos
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Tiempo de Recordatorio",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = "${currentTime} minutos antes",
                onValueChange = { },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                timeOptions.forEach { time ->
                    DropdownMenuItem(
                        text = { 
                            Text("${time} minutos antes")
                        },
                        onClick = {
                            onTimeChange(time)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Sobre las Notificaciones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Solo recibirás notificaciones de tus equipos favoritos\n" +
                          "• Las notificaciones se pueden desactivar en cualquier momento\n" +
                          "• Los recordatorios se envían según el tiempo configurado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
