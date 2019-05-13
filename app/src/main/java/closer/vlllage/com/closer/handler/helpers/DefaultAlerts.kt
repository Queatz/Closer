package closer.vlllage.com.closer.handler.helpers

import android.support.annotation.StringRes
import android.view.View
import android.widget.TextView

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.pool.PoolMember

class DefaultAlerts : PoolMember() {

    @JvmOverloads
    fun thatDidntWork(message: String? = null) {
        `$`(AlertHandler::class.java).make().apply {
                    title = `$`(ResourcesHandler::class.java).resources.getString(R.string.that_didnt_work)
                    this.message = message
                    positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.ok)
                    show()
                }
    }

    fun syncError() {
        `$`(AlertHandler::class.java).make().apply {
            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.sync_didnt_work)
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.boo)
            show()
        }
    }

    fun longMessage(@StringRes title: Int?, @StringRes message: Int) {
        longMessage(title, message)
    }

    fun longMessage(@StringRes title: Int?, message: CharSequence) {
        `$`(AlertHandler::class.java).make().apply {
            this.title = if (title == null) null else `$`(ResourcesHandler::class.java).resources.getString(title)
            layoutResId = R.layout.long_message_modal
            onAfterViewCreated = { _, view -> (view.findViewById<View>(R.id.messageText) as TextView).text = message }
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.ok)
            show()
        }
    }

    fun message(message: String) {
        `$`(AlertHandler::class.java).make().apply {
            this.message = message
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.ok)
            show()
        }

    }

    fun message(message: String, buttonCallback: (Any?) -> Unit) {
        `$`(AlertHandler::class.java).make().apply {
            this.message = message
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.ok)
            positiveButtonCallback = buttonCallback
            show()
        }
    }

    fun message(title: String, message: String) {
        `$`(AlertHandler::class.java).make().apply {
            this.title = title
            this.message = message
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.ok)
            show()
        }
    }

    fun message(@StringRes titleRes: Int, @StringRes messageRes: Int) {
        message(`$`(ResourcesHandler::class.java).resources.getString(titleRes),
                `$`(ResourcesHandler::class.java).resources.getString(messageRes))
    }

    fun message(@StringRes stringRes: Int) {
        message(`$`(ResourcesHandler::class.java).resources.getString(stringRes))
    }
}
