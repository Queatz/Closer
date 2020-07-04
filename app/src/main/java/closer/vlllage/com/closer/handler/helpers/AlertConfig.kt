package closer.vlllage.com.closer.handler.helpers

import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import android.view.View
import androidx.annotation.ColorRes

import closer.vlllage.com.closer.R

class AlertConfig(private val showCallback: (AlertConfig) -> Unit) {
    var title: String? = null
    var message: String? = null
    @ColorRes var messageColorResId: Int? = null
    @LayoutRes var layoutResId: Int? = null
    var positiveButton: String? = null
    var positiveButtonCallback: ((alertResult: Any?) -> Unit)? = null
    var negativeButton: String? = null
    var negativeButtonCallback: ((alertResult: Any?) -> Unit)? = null
    var textViewId: Int? = null
    var onTextViewSubmitCallback: ((value: String) -> Unit)? = null
    var onAfterViewCreated: ((alertConfig: AlertConfig, view: View) -> Unit)? = null
    var buttonClickCallback: ((alertResult: Any?) -> Boolean)? = null
    var cancelIsNegative: Boolean = false
    var alertResult: Any? = null
    @StyleRes var theme = R.style.AppTheme_AlertDialog
    var dialog: AlertDialog? = null

    fun show() {
        showCallback.invoke(this)
    }
}
