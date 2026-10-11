package com.example.model

data class TargetApp(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val apkPath: String,
    val targetSdk: Int,
    val isSystemApp: Boolean
)
