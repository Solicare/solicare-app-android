package com.solicare.monitor.presentation.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.solicare.monitor.R

class OneButtonDialog(
    private val context: Context,
    private val title: String,
    private val message: String,
    private val buttonText: String = context.getString(R.string.confirm),
    private val onClick: (() -> Unit)? = null,
    private val isCancelable: Boolean = true
) {
    fun show() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonText) { dialog, _ ->
                dialog.dismiss()
                onClick?.invoke()
            }
            .create()
        dialog.setCancelable(isCancelable)
        dialog.setCanceledOnTouchOutside(isCancelable)
        dialog.show()
    }
}
