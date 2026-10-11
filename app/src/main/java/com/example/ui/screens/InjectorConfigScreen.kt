package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjectorConfigScreen(
    uiState: UiState,
    generatedCommand: String,
    onNiceNameChange: (String) -> Unit,
    onHookMethodChange: (String) -> Unit,
    onSoPathChange: (String) -> Unit,
    onExecute: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ART Injector Configuration") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Selected Target App",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.selectedApp?.appName ?: "None selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.selectedApp?.packageName ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            OutlinedTextField(
                value = uiState.niceName,
                onValueChange = onNiceNameChange,
                label = { Text("Process Nice Name (--nice-name)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nicename_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.hookMethodSig,
                onValueChange = onHookMethodChange,
                label = { Text("Target ART Method Signature") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hook_method_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.nativeSoPath,
                onValueChange = onSoPathChange,
                label = { Text("Native .so Library Path (LD_PRELOAD)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sopath_input"),
                singleLine = true
            )

            // Command Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Generated app_process Command",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = generatedCommand,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("command_preview_text")
                    )
                }
            }

            Button(
                onClick = onExecute,
                enabled = !uiState.isExecuting && uiState.selectedApp != null && uiState.isBinderAlive && uiState.hasPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("execute_injection_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (uiState.isExecuting) "Executing Injection..." else "Launch via Shizuku app_process")
            }

            if (!uiState.isBinderAlive || !uiState.hasPermission) {
                Text(
                    text = "* Shizuku service must be connected and permission granted to execute app_process.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
