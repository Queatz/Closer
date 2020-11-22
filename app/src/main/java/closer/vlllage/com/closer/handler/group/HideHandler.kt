package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.ToastHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Hide
import closer.vlllage.com.closer.store.models.Hide_
import com.queatz.on.On

class HideHandler(private val on: On) {
    fun hide(group: Group) {
        Hide().apply {
            phoneId = on<PersistenceHandler>().phoneId
            groupId = group.id!!
            on<StoreHandler>().store.box(Hide::class).put(this)
        }

        // Trigger an update on the group
        on<StoreHandler>().store.box(Group::class).put(group)

        on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.contact_hidden))
    }

    fun unhide(group: Group) {
        on<StoreHandler>().store.box(Hide::class).query(
                Hide_.phoneId.equal(on<PersistenceHandler>().phoneId!!).and(
                        Hide_.groupId.equal(group.id!!)
                )
        ).build().remove()

        // Trigger an update on the group
        on<StoreHandler>().store.box(Group::class).put(group)

        on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.contact_unhidden))
    }

    fun isHidden(groupId: String) = on<StoreHandler>().store.box(Hide::class).query(
            Hide_.groupId.equal(groupId).and(Hide_.phoneId.equal(on<PersistenceHandler>().phoneId!!))
    ).build().count() > 0L
}