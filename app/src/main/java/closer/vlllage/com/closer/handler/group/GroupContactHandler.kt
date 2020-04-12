package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ToastHandler
import closer.vlllage.com.closer.store.models.GroupContact
import com.queatz.on.On

class GroupContactHandler constructor(private val on: On) {
    fun updateGroupStatus(groupContact: GroupContact, status: String) {
        if (groupContact.groupId == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        on<DisposableHandler>().add(on<ApiHandler>().updateGroupStatus(groupContact.groupId!!, status).subscribe({
            on<ToastHandler>().show(R.string.group_status_changed)
        }, {
            on<DefaultAlerts>().thatDidntWork()
        }))
    }

    fun updateGroupPhoto(groupContact: GroupContact, photo: String) {
        if (groupContact.groupId == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        on<DisposableHandler>().add(on<ApiHandler>().updateGroupPhoto(groupContact.groupId!!, photo).subscribe({
            on<ToastHandler>().show(R.string.group_photo_changed)
        }, {
            on<DefaultAlerts>().thatDidntWork()
        }))
    }

}
