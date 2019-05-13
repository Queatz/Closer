package closer.vlllage.com.closer.handler.map

import android.view.View
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.helpers.AlertHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolMember

class SetNameHandler : PoolMember() {
    @JvmOverloads
    fun modifyName(onNameModifiedCallback: OnNameModifiedCallback? = null, allowSkip: Boolean = false) {
        `$`(AlertHandler::class.java).make().apply {
            layoutResId = R.layout.set_name_modal
            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.update_your_name)
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.update_your_name)
            negativeButton = (if (allowSkip) `$`(ResourcesHandler::class.java).resources.getString(R.string.skip) else null)
            negativeButtonCallback = if (allowSkip)
                    { result -> onNameModifiedCallback?.onNameModified(null) }
                else
                    null
            textViewId = R.id.input
            onTextViewSubmitCallback = { name ->
                    `$`(AccountHandler::class.java).updateName(name)
                    onNameModifiedCallback?.onNameModified(name)
                }
            onAfterViewCreated = { alertConfig, view ->
                    (view.findViewById(R.id.input) as TextView).text = `$`(AccountHandler::class.java).name

                    if (allowSkip) {
                        view.findViewById<View>(R.id.optionalText).visibility = View.VISIBLE
                    }

                }
            show()
        }
    }

    interface OnNameModifiedCallback {
        fun onNameModified(name: String?)
    }
}
