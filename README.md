# Shizuku ART Injector Pro

A prototype Android application built with **Jetpack Compose**, **Android NDK (C++)**, and the **Shizuku API**.

## Overview

Shizuku ART Injector is designed for advanced developers and researchers exploring Android runtime (ART) internals. Instead of launching applications normally through the standard OS zygote/home screen, this tool utilizes **Shizuku** to invoke `app_process` with custom classpaths and inject native `.so` hooking libraries (via `LD_PRELOAD` / JNI) directly into a target process's memory space—all without requiring root access, modified APKs, or virtual space environments (like VirtualXposed).

## Architecture & Features

- **Shizuku Service & Binder Integration**: Verifies Shizuku server status, pings binders, manages permission grants, and executes privileged shell commands via `Shizuku.newProcess()`.
- **Android NDK & C++ (CMake)**: Includes a native library (`shizukuinjector`) providing C++ hooks, system property queries, and runtime method verification.
- **Target App Picker**: Scans all installed packages on the device with version details, target SDKs, and system app filters.
- **`app_process` Configurator**: Dynamically builds shell execution commands with custom process nice-names, target APK classpaths, and native `.so` library preload paths.
- **Real-Time Terminal Logs**: Captures standard output and error streams from the spawned `app_process` session inside a high-performance terminal UI.

## Requirements

- Android 8.0 (API 26) or higher.
- [Shizuku](https://shizuku.rikka.app/) installed and running (via ADB or Wireless Debugging).

## Building from Source

1. Clone the repository.
2. Open the project in Android Studio (Ladybug or newer).
3. Build and run on your target device or emulator.
