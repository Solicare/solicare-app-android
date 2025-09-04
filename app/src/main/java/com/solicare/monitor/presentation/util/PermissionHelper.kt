package com.solicare.monitor.presentation.util

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
    private var lastDeniedPermissions: List<String> = emptyList()

    fun checkAndRequestPermissions() {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isEmpty()) {
            onGranted()
        } else {
            launcher.launch(notGranted.toTypedArray())
        }
    }

    fun handleResult(result: Map<String, Boolean>) {
        val denied = result.filter { !it.value }.map { it.key }
        lastDeniedPermissions = denied
        if (denied.isEmpty()) {
            onGranted()
        } else {
            onDenied()
        }
    }

    fun getLastDeniedPermissions(): List<String> = lastDeniedPermissions
}