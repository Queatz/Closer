package closer.vlllage.com.closer.handler.helpers

import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.group.DirectGroupHandler
import closer.vlllage.com.closer.handler.group.PhysicalGroupHandler
import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On
import io.reactivex.Single

class GroupNameHelper(private val on: On) {
    fun loadName(group: Group, textView: TextView, hint: Boolean = false, callback: (String) -> String) {
        textView.visible = false

        val set: (String) -> Unit = {
            textView.visible = true

            if (hint) textView.hint = callback(it)
            else textView.text = callback(it)
        }

        getName(group).subscribe({
            set(it)
        }, {}).also { on<DisposableHandler>().add(it) }
    }

    fun getName(group: Group): Single<String> {
        return when {
            group.direct -> on<DirectGroupHandler>().getContactName(group.id!!)
            !group.name.isNullOrBlank() -> Single.just(group.name!!)
            group.physical -> on<PhysicalGroupHandler>().physicalGroupName(group)
            else -> Single.just(on<ResourcesHandler>().resources.getString(R.string.unknown))
        }
    }

    fun loadName(groupId: String?, textView: TextView, hint: Boolean = false, callback: (String) -> String) {
        textView.visible = false

        val set: (String) -> Unit = {
            textView.visible = true

            if (hint) textView.hint = callback(it)
            else textView.text = callback(it)
        }

        if (groupId == null) {
            set(on<ResourcesHandler>().resources.getString(R.string.unknown))
        } else {
            on<DataHandler>().getGroup(groupId).subscribe({
                loadName(it, textView, hint, callback)
            }, {
                set(on<ResourcesHandler>().resources.getString(R.string.unknown))
            }).also { on<DisposableHandler>().add(it) }
        }
    }
}