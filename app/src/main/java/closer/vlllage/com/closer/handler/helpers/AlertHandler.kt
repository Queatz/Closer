package closer.vlllage.com.closer.handler.helpers

import android.content.DialogInterface
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import closer.vlllage.com.closer.handler.group.GroupMessageParseHandler
import com.queatz.on.On

class AlertHandler constructor(private val on: On) {

    fun make(): AlertConfig {
        return AlertConfig { showAlertConfig(it) }
    }

    private fun showAlertConfig(alertConfig: AlertConfig) {
        if (!on<ActivityHandler>().isPresent) {
            if (alertConfig.message != null) {
                on<ToastHandler>().show(alertConfig.message!!)
            }
            return
        }

        val dialogBuilder = AlertDialog.Builder(on<ActivityHandler>().activity!!, alertConfig.theme)
        var textView: TextView? = null
        if (alertConfig.layoutResId != null) {
            val view = View.inflate(on<ActivityHandler>().activity, alertConfig.layoutResId!!, null)

            if (alertConfig.textViewId != null) {
                textView = view.findViewById(alertConfig.textViewId!!)
                val finalTextView = textView
                textView?.post { textView.requestFocus() }
                textView?.post { on<KeyboardHandler>().showKeyboard(finalTextView, true) }
                dialogBuilder.setOnDismissListener { on<KeyboardHandler>().showKeyboard(finalTextView, false) }
            }

            alertConfig.onAfterViewCreated?.invoke(alertConfig, view)

            dialogBuilder.setView(view)
        }

        if (alertConfig.positiveButton != null) {
            val finalTextView = textView
            dialogBuilder.setPositiveButton(alertConfig.positiveButton) { d, w ->
                if (finalTextView != null) {
                    alertConfig.onTextViewSubmitCallback?.invoke(finalTextView.text.toString())
                }

                alertConfig.positiveButtonCallback?.invoke(alertConfig.alertResult)
            }
        }

        if (alertConfig.negativeButton != null) {
            dialogBuilder.setNegativeButton(alertConfig.negativeButton) { d, w ->
                alertConfig.negativeButtonCallback?.invoke(alertConfig.alertResult)
            }
        }

        if (alertConfig.message != null) {
            dialogBuilder.setMessage(on<GroupMessageParseHandler>().parseString(alertConfig.message!!, prefix = ""))
        }

        if (alertConfig.title != null) {
            dialogBuilder.setTitle(alertConfig.title)
        }

        val alertDialog = dialogBuilder.create()

        textView?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick()
                return@setOnEditorActionListener true
            }

            false
        }

        alertConfig.buttonClickCallback?.let {
            val finalTextView = textView
            alertDialog.setOnShowListener { dialog ->
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener { view ->
                    if (it.invoke(alertConfig.alertResult)) {
                        if (finalTextView != null) {
                            alertConfig.onTextViewSubmitCallback?.invoke(finalTextView.text.toString())
                        }

                        alertConfig.positiveButtonCallback?.invoke(alertConfig.alertResult)
                        dialog.dismiss()
                    }
                }
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener { view ->
                    if (it.invoke(alertConfig.alertResult)) {
                        alertConfig.negativeButtonCallback?.invoke(alertConfig.alertResult)
                        dialog.dismiss()
                    }
                }
            }
        }

        alertConfig.dialog = alertDialog

        alertDialog.show()
    }
}
