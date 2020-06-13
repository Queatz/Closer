package closer.vlllage.com.closer.handler.helpers

import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.group.PhysicalGroupHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.kotlin.single

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

    fun loadName(groupId: String?, textView: TextView, callback: (String) -> String) {
        if (groupId == null) {
            textView.visible = true
            textView.text = callback(on<ResourcesHandler>().resources.getString(R.string.unknown))
        } else {
            on<StoreHandler>().store.box(Group::class).query()
                    .equal(Group_.id, groupId)
                    .build()
                    .subscribe()
                    .single()
                    .on(AndroidScheduler.mainThread())
                    .observer {
                        if (it.isEmpty()) {
                            textView.visible = true
                            textView.text = callback(on<ResourcesHandler>().resources.getString(R.string.unknown))
                        } else {
                            loadName(it.first(), textView, callback)
                        }
                    }.also { on<DisposableHandler>().add(it) }

        }
    }
}