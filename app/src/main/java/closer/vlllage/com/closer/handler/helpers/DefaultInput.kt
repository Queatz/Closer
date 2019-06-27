package closer.vlllage.com.closer.handler.helpers

import android.widget.TextView
import androidx.annotation.StringRes
import closer.vlllage.com.closer.R
import com.queatz.on.On

class DefaultInput constructor(private val on: On) {
    fun show(@StringRes titleRes: Int,
             @StringRes hintRes: Int = R.string.say_something,
             @StringRes button: Int = R.string.ok,
             prefill: String? = null,
             callback: (String) -> Unit) {
        on<AlertHandler>().make().apply {
            layoutResId = R.layout.simple_input_modal
            textViewId = R.id.input
            onAfterViewCreated = { _, view ->
                val input = view.findViewById<TextView>(R.id.input)
                input.setHint(hintRes)
                input.text = prefill ?: ""
            }
            onTextViewSubmitCallback = callback
            title = on<ResourcesHandler>().resources.getString(titleRes)
            positiveButton = on<ResourcesHandler>().resources.getString(button)
            show()
        }
    }
}
