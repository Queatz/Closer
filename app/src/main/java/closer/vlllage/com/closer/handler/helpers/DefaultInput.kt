package closer.vlllage.com.closer.handler.helpers

import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import closer.vlllage.com.closer.R
import com.queatz.on.On
import kotlinx.android.synthetic.main.simple_input_modal.view.*
import kotlinx.android.synthetic.main.simple_two_input_modal.view.*

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
                val input: TextView = view.input
                input.setHint(hintRes)
                input.text = prefill ?: ""
            }
            onTextViewSubmitCallback = callback
            title = on<ResourcesHandler>().resources.getString(titleRes)
            positiveButton = on<ResourcesHandler>().resources.getString(button)
            show()
        }
    }

    fun showWithDesc(@StringRes titleRes: Int,
             @StringRes hintRes: Int = R.string.say_something,
             @StringRes hintTwoRes: Int = R.string.say_something,
             @StringRes button: Int = R.string.ok,
             prefill: String? = null,
             prefillTwo: String? = null,
             callback: (String, String) -> Unit,
             buttonCallback: ((String, String) -> Boolean)?) {
        on<AlertHandler>().make().apply {
            layoutResId = R.layout.simple_two_input_modal
            onAfterViewCreated = { _, view ->
                val inputOne: TextView = view.inputOne
                val inputTwo: TextView = view.inputTwo
                inputOne.setHint(hintRes)
                inputOne.text = prefill ?: ""
                inputTwo.setHint(hintTwoRes)
                inputTwo.text = prefillTwo ?: ""
                alertResult = view
            }
            positiveButtonCallback = {
                (it as ViewGroup).apply {
                    callback(
                            inputOne.text.toString(),
                            inputTwo.text.toString()
                    )
                }
            }
            buttonClickCallback = {
                (it as ViewGroup).let { alertResult ->
                    buttonCallback?.invoke(
                            alertResult.inputOne.text.toString(),
                            alertResult.inputTwo.text.toString()
                    ) ?: true
                }
            }
            title = on<ResourcesHandler>().resources.getString(titleRes)
            positiveButton = on<ResourcesHandler>().resources.getString(button)
            show()
        }
    }
}
