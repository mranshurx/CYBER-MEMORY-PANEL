package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How Shizuku & app_process Works") },
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
            TutorialSection(
                title = "1. What is Shizuku?",
                content = "Shizuku allows an app to use system APIs with privileges of the adb or root user without rooting the device. It communicates via a privileged binder service started via adb or wireless debugging."
            )

            TutorialSection(
                title = "2. The Role of app_process",
                content = "Instead of launching an Android app normally via the home screen or standard Android OS zygote, `app_process` is a native binary that can bootstrap the Android Runtime (ART) from a shell environment. By invoking `app_process` with a target APK's classpath, you can run the app's package under custom control."
            )

            TutorialSection(
                title = "3. Native Library & ART Hooking (.so)",
                content = "By leveraging `LD_PRELOAD` or native JNI linkage, native `.so` hooking libraries can be mapped directly into the running process memory space during startup. This allows intercepting methods and modifying runtime behavior without requiring virtual spaces (like VirtualXposed) or repackaging/signing modified APKs."
            )

            TutorialSection(
                title = "4. Prototype Usage Workflow",
                content = "1. Ensure Shizuku is running and permission is granted in Shizuku app.\n2. Pick a target installed application in the App Picker.\n3. Configure ART hook method signature and native library path.\n4. Execute `app_process` via Shizuku (`Shizuku.newProcess`) and inspect real-time terminal logs."
            )
        }
    }
}

@Composable
fun TutorialSection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
