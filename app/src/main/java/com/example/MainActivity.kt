package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shizuku.ShizukuManager
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuManager.PERMISSION_REQUEST_CODE) {
            viewModel.refreshShizukuStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            Shizuku.addRequestPermissionResultListener(permissionListener)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val uiState by viewModel.uiState.collectAsState()

                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                uiState = uiState,
                                onRefreshShizuku = { viewModel.refreshShizukuStatus() },
                                onRequestPermission = { viewModel.requestShizukuPermission() },
                                onNavigateToPicker = { navController.navigate("picker") },
                                onNavigateToConfig = { navController.navigate("config") },
                                onNavigateToLogs = { navController.navigate("logs") },
                                onNavigateToTutorial = { navController.navigate("tutorial") }
                            )
                        }
                        composable("picker") {
                            AppPickerScreen(
                                installedApps = uiState.installedApps,
                                selectedApp = uiState.selectedApp,
                                onSelectApp = { viewModel.selectApp(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("config") {
                            InjectorConfigScreen(
                                uiState = uiState,
                                generatedCommand = viewModel.generateAppProcessCommand(),
                                onNiceNameChange = { viewModel.updateNiceName(it) },
                                onHookMethodChange = { viewModel.updateHookMethod(it) },
                                onSoPathChange = { viewModel.updateSoPath(it) },
                                onExecute = {
                                    viewModel.executeInjection()
                                    navController.navigate("logs")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("logs") {
                            LogsScreen(
                                logs = uiState.executionLogs,
                                onClearLogs = { viewModel.clearLogs() },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("tutorial") {
                            TutorialScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Shizuku.removeRequestPermissionResultListener(permissionListener)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
