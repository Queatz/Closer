package closer.vlllage.com.closer.handler.helpers

import androidx.annotation.StringRes
import android.view.View
import android.widget.TextView

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.LongMessageModalBinding
import com.queatz.on.On

class DefaultAlerts constructor(private val on: On) {

    fun thatDidntWork(message: String? = null) {
        on<AlertHandler>().make().apply {
                    title = on<ResourcesHandler>().resources.getString(R.string.that_didnt_work)
                    this.message = message
                    positiveButton = on<ResourcesHandler>().resources.getString(R.string.ok)
                    show()
                }
    }

    fun syncError() {
        on<AlertHandler>().make().apply {
            title = on<ResourcesHandler>().resources.getString(R.string.sync_didnt_work)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.boo)
            show()
        }
    }

    fun longMessage(@StringRes title: Int?, @StringRes message: Int) {
        longMessage(title, message)
    }

    fun longMessage(@StringRes title: Int?, message: CharSequence) {
        on<AlertHandler>().view { LongMessageModalBinding.inflate(it) }.apply {
            this.title = if (title == null) null else on<ResourcesHandler>().resources.getString(title)
            onAfterViewCreated = { _, view -> view.messageText.text = message }
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.ok)
            show()
        }
    }

    fun message(message: String) {
        on<AlertHandler>().make().apply {
            this.message = message
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.ok)
            show()
        }

    }

    fun message(message: String, buttonCallback: (Any?) -> Unit) {
        on<AlertHandler>().make().apply {
            this.message = message
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.ok)
            positiveButtonCallback = buttonCallback
            show()
        }
    }

    fun message(title: String, message: String, closeCallback: (() -> Unit)? = null) {
        on<AlertHandler>().make().apply {
            this.title = title
            this.message = message
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.ok)
            positiveButtonCallback = { closeCallback?.invoke() }
            negativeButtonCallback = { closeCallback?.invoke() }
            cancelIsNegative = true
            show()
        }
    }

    fun message(@StringRes titleRes: Int, @StringRes messageRes: Int) {
        message(on<ResourcesHandler>().resources.getString(titleRes),
                on<ResourcesHandler>().resources.getString(messageRes))
    }

    fun message(@StringRes stringRes: Int) {
        message(on<ResourcesHandler>().resources.getString(stringRes))
    }
}
