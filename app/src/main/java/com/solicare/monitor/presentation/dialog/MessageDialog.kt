package com.solicare.monitor.presentation.dialog

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog

class MessageDialog(
    private val context: Context,
    private val title: String,
    private val message: String,
    private val durationMillis: Long = 1500L // 자동 닫힘 시간 (기본 1.5초)
) {
    fun show() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .create()
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) dialog.dismiss()
        }, durationMillis)
    }
}

