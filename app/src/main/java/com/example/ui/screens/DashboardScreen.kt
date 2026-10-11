package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: UiState,
    onRefreshShizuku: () -> Unit,
    onRequestPermission: () -> Unit,
    onNavigateToPicker: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToTutorial: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shizuku ART Injector") },
                actions = {
                    IconButton(
                        onClick = onRefreshShizuku,
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Status")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shizuku Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shizuku_status_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isBinderAlive && uiState.hasPermission)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shizuku Service Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (uiState.isBinderAlive && uiState.hasPermission)
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.Warning,
                            contentDescription = "Status Icon"
                        )
                    }

                    Divider()

                    StatusRow(label = "Installed", value = if (uiState.isShizukuInstalled) "Yes" else "No")
                    StatusRow(label = "Binder Alive", value = if (uiState.isBinderAlive) "Connected" else "Disconnected")
                    StatusRow(label = "Permission", value = if (uiState.hasPermission) "Granted" else "Not Granted")
                    StatusRow(label = "Shizuku Version", value = if (uiState.shizukuVersion >= 0) "${uiState.shizukuVersion}" else "Unknown")
                    StatusRow(label = "Process UID", value = if (uiState.shizukuUid >= 0) "${uiState.shizukuUid}" else "N/A")

                    if (!uiState.hasPermission && uiState.isBinderAlive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("request_permission_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Request Shizuku Permission")
                        }
                    }
                }
            }

            // NDK Native Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ndk_status_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "NDK & ART Hook Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = uiState.nativeStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Target App Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("target_app_card"),
                onClick = onNavigateToPicker
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Target Application",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Select App")
                    }

                    if (uiState.selectedApp != null) {
                        Text(
                            text = uiState.selectedApp.appName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Package: ${uiState.selectedApp.packageName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        Text(
                            text = "No target app selected. Tap to choose.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Quick Actions Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToConfig,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("goto_config_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Configure")
                }

                Button(
                    onClick = onNavigateToLogs,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("goto_logs_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Logs (${uiState.executionLogs.size})")
                }
            }

            OutlinedButton(
                onClick = onNavigateToTutorial,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goto_tutorial_button")
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("How Shizuku + app_process Works")
            }
        }
    }
}

@Composable
fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
