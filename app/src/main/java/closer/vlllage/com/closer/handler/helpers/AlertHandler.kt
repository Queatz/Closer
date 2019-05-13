package closer.vlllage.com.closer.handler.helpers

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView

import closer.vlllage.com.closer.pool.PoolMember

class AlertHandler : PoolMember() {

    fun make(): AlertConfig {
        return AlertConfig { showAlertConfig(it) }
    }

    private fun showAlertConfig(alertConfig: AlertConfig) {
        if (!`$`(ActivityHandler::class.java).isPresent) {
            if (alertConfig.message != null) {
                `$`(ToastHandler::class.java).show(alertConfig.message!!)
            }
            return
        }

        val dialogBuilder = AlertDialog.Builder(`$`(ActivityHandler::class.java).activity!!, alertConfig.theme)
        var textView: TextView? = null
        if (alertConfig.layoutResId != null) {
            val view = View.inflate(`$`(ActivityHandler::class.java).activity, alertConfig.layoutResId!!, null)

            if (alertConfig.textViewId != null) {
                textView = view.findViewById(alertConfig.textViewId!!)
                val finalTextView = textView
                textView!!.post { textView.requestFocus() }
                textView.post { `$`(KeyboardHandler::class.java).showKeyboard(finalTextView, true) }
                dialogBuilder.setOnDismissListener { dialogInterface -> `$`(KeyboardHandler::class.java).showKeyboard(finalTextView, false) }
            }

            if (alertConfig.onAfterViewCreated != null) {
                alertConfig.onAfterViewCreated!!.invoke(alertConfig, view)
            }

            dialogBuilder.setView(view)
        }

        if (alertConfig.positiveButton != null) {
            val finalTextView = textView
            dialogBuilder.setPositiveButton(alertConfig.positiveButton) { d, w ->
                if (alertConfig.onTextViewSubmitCallback != null && finalTextView != null) {
                    alertConfig.onTextViewSubmitCallback!!.invoke(finalTextView.text.toString())
                }

                if (alertConfig.positiveButtonCallback != null) {
                    alertConfig.positiveButtonCallback!!.invoke(alertConfig.alertResult)
                }
            }
        }

        if (alertConfig.negativeButton != null) {
            dialogBuilder.setNegativeButton(alertConfig.negativeButton) { d, w ->
                if (alertConfig.negativeButtonCallback != null) {
                    alertConfig.negativeButtonCallback!!.invoke(alertConfig.alertResult)
                }
            }
        }

        if (alertConfig.message != null) {
            dialogBuilder.setMessage(alertConfig.message)
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

        if (alertConfig.buttonClickCallback != null) {
            alertDialog.setOnShowListener { dialog ->
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener { view ->
                    if (alertConfig.buttonClickCallback!!.invoke(alertConfig.alertResult)) {
                        if (alertConfig.positiveButtonCallback != null) {
                            alertConfig.positiveButtonCallback!!.invoke(alertConfig.alertResult)
                            dialog.dismiss()
                        }
                    }
                }
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener { view ->
                    if (alertConfig.buttonClickCallback!!.invoke(alertConfig.alertResult)) {
                        if (alertConfig.negativeButtonCallback != null) {
                            alertConfig.negativeButtonCallback!!.invoke(alertConfig.alertResult)
                            dialog.dismiss()
                        }
                    }
                }
            }
        }

        alertConfig.dialog = alertDialog

        alertDialog.show()
    }
}
