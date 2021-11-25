package closer.vlllage.com.closer.handler.helpers

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import closer.vlllage.com.closer.handler.group.GroupMessageParseHandler
import com.queatz.on.On


class AlertHandler constructor(private val on: On) {

    fun <T : ViewBinding>view(inflate: (LayoutInflater) -> T): AlertConfig<T> {
        return make(inflate(on<ActivityHandler>().activity!!.layoutInflater))
    }

    fun <T : ViewBinding>make(binding: T): AlertConfig<T> {
        return AlertConfig { showAlertConfig(binding, it) }
    }

    fun make(): AlertConfig<ViewBinding> {
        return AlertConfig { showAlertConfig(null, it) }
    }

    private fun <T : ViewBinding>showAlertConfig(binding: T?, alertConfig: AlertConfig<T>) {
        if (!on<ActivityHandler>().isPresent) {
            if (alertConfig.message != null) {
                on<ToastHandler>().show(alertConfig.message!!)
            }
            return
        }

        if (alertConfig.message != null) {
            on<GroupMessageParseHandler>().parseString(alertConfig.message!!, prefix = "").subscribe({
                show(binding, alertConfig, it)
            }, {
                show(binding, alertConfig, alertConfig.message)
            }).also {
                on<DisposableHandler>().add(it)
            }
        } else {
            show(binding, alertConfig, alertConfig.message)
        }
    }

    private fun <T : ViewBinding>show(binding: T?, alertConfig: AlertConfig<T>, parsedMessage: String?) {
        val dialogBuilder = AlertDialog.Builder(on<ActivityHandler>().activity!!, alertConfig.theme)
        var textView: TextView? = null

        if (binding != null) {
            if (alertConfig.textViewId != null) {
                textView = binding.root.findViewById(alertConfig.textViewId!!)
                val finalTextView = textView
                textView?.post { textView.requestFocus() }
                textView?.post { on<KeyboardHandler>().showKeyboard(finalTextView, true) }
                dialogBuilder.setOnDismissListener { on<KeyboardHandler>().showKeyboard(finalTextView, false) }
            }

            alertConfig.onAfterViewCreated?.invoke(alertConfig, binding)

            dialogBuilder.setView(binding.root)
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

        if (parsedMessage != null) {
            dialogBuilder.setMessage(parsedMessage)
        }

        if (alertConfig.title != null) {
            dialogBuilder.setTitle(alertConfig.title)
        }

        if (alertConfig.cancelIsNegative) {
            dialogBuilder.setOnCancelListener {
                alertConfig.negativeButtonCallback?.invoke(alertConfig.alertResult)
            }
        }

        if (alertConfig.iconResId != null) {
            dialogBuilder.setIcon(alertConfig.iconResId!!)
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
                    if (it.invoke(alertConfig.alertResult, DialogInterface.BUTTON_POSITIVE)) {
                        if (finalTextView != null) {
                            alertConfig.onTextViewSubmitCallback?.invoke(finalTextView.text.toString())
                        }

                        alertConfig.positiveButtonCallback?.invoke(alertConfig.alertResult)
                        dialog.dismiss()
                    }
                }
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener { view ->
                    if (it.invoke(alertConfig.alertResult, DialogInterface.BUTTON_NEGATIVE)) {
                        alertConfig.negativeButtonCallback?.invoke(alertConfig.alertResult)
                        dialog.dismiss()
                    }
                }
            }
        }

        alertConfig.dialog = alertDialog

        alertDialog.show()

        alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }
}
