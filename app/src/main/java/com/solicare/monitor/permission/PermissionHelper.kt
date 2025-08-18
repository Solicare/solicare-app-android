package com.solicare.monitor.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class PermissionHelper(
    private val context: Context,
    private val launcher: ActivityResultLauncher<Array<String>>,
    private val permissions: Array<String>,
    private val onGranted: () -> Unit,
    private val onDenied: () -> Unit
) {
    fun checkAndRequestPermissions() {
        if (permissions.all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            onGranted()
        } else {
            launcher.launch(permissions)
        }
    }

    fun handleResult(result: Map<String, Boolean>) {
        if (result.all { it.value }) {
            onGranted()
        } else {
            onDenied()
        }
    }
}

