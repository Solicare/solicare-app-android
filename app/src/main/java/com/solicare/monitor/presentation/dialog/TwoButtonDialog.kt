package com.solicare.monitor.presentation.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.solicare.monitor.R

class TwoButtonDialog(
    private val context: Context,
    private val title: String,
    private val message: String,
    private val positiveText: String = context.getString(R.string.confirm),
    private val negativeText: String = context.getString(R.string.cancel),
    private val onPositive: (() -> Unit)? = null,
    private val onNegative: (() -> Unit)? = null,
    private val isCancelable: Boolean = true
) {
    fun show() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                dialog.dismiss()
                onPositive?.invoke()
            }
            .setNegativeButton(negativeText) { dialog, _ ->
                dialog.dismiss()
                onNegative?.invoke()
            }
            .create()
        dialog.setCancelable(isCancelable)
        dialog.setCanceledOnTouchOutside(isCancelable)
        dialog.show()
    }
}
