package com.example.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.TargetApp
import com.example.native.NativeBridge
import com.example.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val isShizukuInstalled: Boolean = false,
    val isBinderAlive: Boolean = false,
    val hasPermission: Boolean = false,
    val shizukuVersion: Int = -1,
    val shizukuUid: Int = -1,
    val installedApps: List<TargetApp> = emptyList(),
    val selectedApp: TargetApp? = null,
    val niceName: String = "hooked_process",
    val hookMethodSig: String = "Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V",
    val nativeSoPath: String = "/data/local/tmp/libshizukuinjector.so",
    val executionLogs: List<String> = emptyList(),
    val isExecuting: Boolean = false,
    val nativeStatus: String = "Initializing NDK..."
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val nativeBridge = NativeBridge()

    init {
        refreshShizukuStatus()
        loadInstalledApps()
        checkNativeStatus()
    }

    fun refreshShizukuStatus() {
        val pm = getApplication<Application>().packageManager
        val installed = ShizukuManager.isShizukuInstalled(pm)
        val binderAlive = ShizukuManager.isBinderAlive()
        val version = ShizukuManager.getVersion()
        val uid = ShizukuManager.getUid()
        val permission = ShizukuManager.hasPermission()

        _uiState.value = _uiState.value.copy(
            isShizukuInstalled = installed,
            isBinderAlive = binderAlive,
            shizukuVersion = version,
            shizukuUid = uid,
            hasPermission = permission
        )
    }

    fun requestShizukuPermission() {
        ShizukuManager.requestPermission()
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            refreshShizukuStatus()
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            val apps = packages.mapNotNull { pkgInfo ->
                val appInfo = pkgInfo.applicationInfo ?: return@mapNotNull null
                val appName = pm.getApplicationLabel(appInfo).toString()
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                TargetApp(
                    appName = appName,
                    packageName = pkgInfo.packageName ?: "",
                    versionName = pkgInfo.versionName ?: "1.0",
                    versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        pkgInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        pkgInfo.versionCode.toLong()
                    },
                    apkPath = appInfo.sourceDir ?: "",
                    targetSdk = appInfo.targetSdkVersion,
                    isSystemApp = isSystem
                )
            }.sortedBy { it.appName }

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(installedApps = apps)
                if (_uiState.value.selectedApp == null && apps.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(selectedApp = apps.first())
                }
            }
        }
    }

    fun selectApp(app: TargetApp) {
        _uiState.value = _uiState.value.copy(
            selectedApp = app,
            niceName = "${app.packageName.substringAfterLast('.')}_hooked"
        )
    }

    fun updateNiceName(name: String) {
        _uiState.value = _uiState.value.copy(niceName = name)
    }

    fun updateHookMethod(sig: String) {
        _uiState.value = _uiState.value.copy(hookMethodSig = sig)
    }

    fun updateSoPath(path: String) {
        _uiState.value = _uiState.value.copy(nativeSoPath = path)
    }

    fun checkNativeStatus() {
        try {
            val status = nativeBridge.getNativeStatus()
            _uiState.value = _uiState.value.copy(nativeStatus = status)
        } catch (e: Throwable) {
            _uiState.value = _uiState.value.copy(nativeStatus = "NDK Error: ${e.message}")
        }
    }

    fun generateAppProcessCommand(): String {
        val app = _uiState.value.selectedApp ?: return "echo 'No target app selected'"
        val classpath = app.apkPath
        val niceName = _uiState.value.niceName
        val soPath = _uiState.value.nativeSoPath

        // Build robust app_process command executing under Shizuku
        // export LD_PRELOAD=$soPath or ART hooking bootstrap
        return "export CLASSPATH=\"$classpath\"\n" +
               "export LD_PRELOAD=\"$soPath\"\n" +
               "echo '[*] Injecting ART Runtime Hook into ${app.packageName}'\n" +
               "echo '[*] Using APK source: $classpath'\n" +
               "echo '[*] Loading native library: $soPath'\n" +
               "exec /system/bin/app_process /system/bin --nice-name=\"$niceName\" com.android.internal.os.RuntimeInit --application"
    }

    val logs = MutableStateFlow<List<String>>(emptyList())

    fun executeInjection() {
        val cmd = generateAppProcessCommand()
        val currentLogs = _uiState.value.executionLogs.toMutableList()
        currentLogs.add("--- Starting Injection Session ---")
        currentLogs.add("Executing via Shizuku newProcess():")
        currentLogs.add(cmd)
        
        _uiState.value = _uiState.value.copy(
            executionLogs = currentLogs,
            isExecuting = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            // Test native ART hook jni
            try {
                val jniSuccess = nativeBridge.injectArtHook(_uiState.value.hookMethodSig)
                withContext(Dispatchers.Main) {
                    val logsPlus = _uiState.value.executionLogs.toMutableList()
                    logsPlus.add("[JNI] ART hook method verification: $jniSuccess")
                    _uiState.value = _uiState.value.copy(executionLogs = logsPlus)
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    val logsPlus = _uiState.value.executionLogs.toMutableList()
                    logsPlus.add("[JNI] Warning: ${e.message}")
                    _uiState.value = _uiState.value.copy(executionLogs = logsPlus)
                }
            }

            val exitCode = ShizukuManager.executeAppProcessCommand(cmd) { line ->
                viewModelScope.launch(Dispatchers.Main) {
                    val l = _uiState.value.executionLogs.toMutableList()
                    l.add(line)
                    _uiState.value = _uiState.value.copy(executionLogs = l)
                }
            }

            withContext(Dispatchers.Main) {
                val l = _uiState.value.executionLogs.toMutableList()
                l.add("--- Process Exited with code $exitCode ---")
                _uiState.value = _uiState.value.copy(
                    executionLogs = l,
                    isExecuting = false
                )
            }
        }
    }

    fun clearLogs() {
        _uiState.value = _uiState.value.copy(executionLogs = emptyList())
    }
}
