package com.example.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method

object ShizukuManager {
    const val PERMISSION_REQUEST_CODE = 1001

    fun isShizukuInstalled(packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isBinderAlive(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    fun getVersion(): Int {
        return try {
            Shizuku.getVersion()
        } catch (e: Throwable) {
            -1
        }
    }

    fun getUid(): Int {
        return try {
            Shizuku.getUid()
        } catch (e: Throwable) {
            -1
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11()) {
                true
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Throwable) {
            false
        }
    }

    fun requestPermission() {
        try {
            if (!Shizuku.isPreV11() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun executeAppProcessCommand(command: String, onOutput: (String) -> Unit): Int {
        try {
            // Use reflection to invoke Shizuku.newProcess safely across all versions
            val method: Method = Shizuku::class.java.getMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            Thread {
                var line: String?
                while (true) {
                    line = reader.readLine()
                    if (line == null) break
                    onOutput("[OUT] $line")
                }
            }.start()

            Thread {
                var line: String?
                while (true) {
                    line = errorReader.readLine()
                    if (line == null) break
                    onOutput("[ERR] $line")
                }
            }.start()

            return process.waitFor()
        } catch (e: Throwable) {
            onOutput("[EXC] ${e.message}")
            return -1
        }
    }
}
