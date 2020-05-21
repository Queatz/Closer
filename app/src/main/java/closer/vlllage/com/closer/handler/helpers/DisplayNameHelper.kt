package closer.vlllage.com.closer.handler.helpers

import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.group.PhysicalGroupHandler
import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On

class DisplayNameHelper(private val on: On) {
    fun loadName(group: Group, textView: TextView, callback: (String) -> String) {
        textView.visible = false

        if (!group.name.isNullOrBlank()) {
            textView.visible = true
            textView.text = callback(group.name!!)
        } else if (group.physical) {
            on<PhysicalGroupHandler>().physicalGroupName(group).subscribe({
                if (textView.isAttachedToWindow) {
                    textView.visible = true
                    textView.text = callback(it)
                }
            }, {}).also { on<DisposableHandler>().add(it) }
        } else {
            textView.visible = true
            textView.text = callback(on<ResourcesHandler>().resources.getString(R.string.unknown))
        }
    }
}