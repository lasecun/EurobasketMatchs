package es.itram.basketmatch.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.itram.basketmatch.BuildConfig
import es.itram.basketmatch.R
import es.itram.basketmatch.ui.theme.TTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onSyncSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                // Section Header - Material Design pattern
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.general),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            item {
                // Settings Item using Material Design ListItem
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.sync_settings),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(R.string.sync_settings_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { onSyncSettingsClick() },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
            
            item {
                // Spacer to push version to bottom
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            item {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }
            
            item {
                VersionInfo()
            }
        }
    }
}

@Composable
private fun VersionInfo(
    modifier: Modifier = Modifier
) {
    // Material Design version info using ListItem
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = stringResource(R.string.version_format, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.build_format, BuildConfig.VERSION_CODE),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    TTheme {
        SettingsScreen(
            onBackClick = {},
            onSyncSettingsClick = {}
        )
    }
}
