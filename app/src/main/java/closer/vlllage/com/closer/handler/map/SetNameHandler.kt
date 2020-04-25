package closer.vlllage.com.closer.handler.map

import android.view.View
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.helpers.AlertHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.queatz.on.On

class SetNameHandler constructor(private val on: On) {
    @JvmOverloads
    fun modifyName(onNameModifiedCallback: OnNameModifiedCallback? = null, allowSkip: Boolean = false) {
        on<AlertHandler>().make().apply {
            layoutResId = R.layout.set_name_modal
            title = on<ResourcesHandler>().resources.getString(R.string.update_your_name)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.update_your_name)
            negativeButton = (if (allowSkip) on<ResourcesHandler>().resources.getString(R.string.skip) else null)
            negativeButtonCallback = if (allowSkip)
                    { result -> onNameModifiedCallback?.onNameModified(null) }
                else
                    null
            textViewId = R.id.input
            onTextViewSubmitCallback = { name ->
                    on<AccountHandler>().updateName(name)
                    onNameModifiedCallback?.onNameModified(name)
                }
            onAfterViewCreated = { alertConfig, view ->
                    (view.findViewById(R.id.input) as TextView).text = on<AccountHandler>().name

                    if (allowSkip) {
                        view.findViewById<View>(R.id.optionalText).visible = true
                    }

                }
            show()
        }
    }

    interface OnNameModifiedCallback {
        fun onNameModified(name: String?)
    }
}
