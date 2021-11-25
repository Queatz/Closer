package closer.vlllage.com.closer.handler.helpers

import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.SimpleInputModalBinding
import closer.vlllage.com.closer.databinding.SimpleTwoInputModalBinding
import com.queatz.on.On


class DefaultInput constructor(private val on: On) {
    fun show(@StringRes titleRes: Int,
             @StringRes hintRes: Int = R.string.say_something,
             @StringRes buttonRes: Int = R.string.ok,
             prefill: String? = null,
             inputType: Int? = null,
             multiline: Boolean = false,
             @StyleRes themeRes: Int? = null,
             maxLength: Int? = null,
             callback: (String) -> Unit) {
        show(
                on<ResourcesHandler>().resources.getString(titleRes),
                on<ResourcesHandler>().resources.getString(hintRes),
                on<ResourcesHandler>().resources.getString(buttonRes),
                prefill,
                inputType,
                multiline,
                themeRes,
                maxLength,
                callback
        )
    }

    fun show(titleString: String,
             hint: String? = null,
             button: String? = null,
             prefill: String? = null,
             inputType: Int? = null,
             multiline: Boolean = false,
             @StyleRes themeRes: Int? = null,
             maxLength: Int? = null,
             callback: (String) -> Unit) {
        on<AlertHandler>().view { SimpleInputModalBinding.inflate(it) }.apply {
            themeRes?.let { theme = it }
            textViewId = R.id.input
            onAfterViewCreated = { _, view ->
                val input: EditText = view.input
                input.hint = hint
                input.setText(prefill ?: "")
                inputType?.let { input.inputType = it }
                if (multiline) {
                    input.inputType = input.inputType or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    input.maxLines = Integer.MAX_VALUE
                }

                if (maxLength != null) {
                    input.filters = arrayOf<InputFilter>(LengthFilter(maxLength))
                }
            }
            onTextViewSubmitCallback = callback
            title = titleString
            positiveButton = button
            show()
        }
    }



    fun showWithDesc(@StringRes titleRes: Int,
             @StringRes hintRes: Int = R.string.say_something,
             @StringRes hintTwoRes: Int = R.string.say_something,
             @StringRes button: Int = R.string.ok,
             prefill: String? = null,
             prefillTwo: String? = null,
             @StyleRes theme: Int? = null,
             callback: (String, String) -> Unit,
             buttonCallback: ((String, String) -> Boolean)?) {
        on<AlertHandler>().view { SimpleTwoInputModalBinding.inflate(it) }.apply {
            theme?.let { this.theme = it }
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
                (it as SimpleTwoInputModalBinding).let { alertResult ->
                    callback(
                        alertResult.inputOne.text.toString(),
                        alertResult.inputTwo.text.toString()
                    )
                }
            }
            buttonClickCallback = { it, _ ->
                (it as SimpleTwoInputModalBinding).let { alertResult ->
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
