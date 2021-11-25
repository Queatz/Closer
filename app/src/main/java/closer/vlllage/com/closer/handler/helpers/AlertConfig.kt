package closer.vlllage.com.closer.handler.helpers

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import closer.vlllage.com.closer.R

class AlertConfig<T : ViewBinding>(private val showCallback: (AlertConfig<T>) -> Unit) {
    var title: String? = null
    var message: String? = null
    @ColorRes var messageColorResId: Int? = null
    var binding: T? = null
    @DrawableRes var iconResId: Int? = null
    var positiveButton: String? = null
    var positiveButtonCallback: ((alertResult: Any?) -> Unit)? = null
    var negativeButton: String? = null
    var negativeButtonCallback: ((alertResult: Any?) -> Unit)? = null
    var textViewId: Int? = null
    var onTextViewSubmitCallback: ((value: String) -> Unit)? = null
    var onAfterViewCreated: ((alertConfig: AlertConfig<T>, view: T) -> Unit)? = null
    var buttonClickCallback: ((alertResult: Any?, buttonId: Int) -> Boolean)? = null
    var cancelIsNegative: Boolean = false
    var alertResult: Any? = null
    @StyleRes var theme = R.style.AppTheme_AlertDialog
    var dialog: AlertDialog? = null

    fun show() {
        showCallback.invoke(this)
    }
}
