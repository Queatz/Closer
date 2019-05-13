package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group

class PhysicalGroupUpgradeHandler : PoolMember() {
    fun convertToHub(group: Group, onGroupUpdateListener: (Group) -> Unit) {
        `$`(AlertHandler::class.java).make().apply {
            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.set_name)
            layoutResId = R.layout.input_modal
            textViewId = R.id.input
            onTextViewSubmitCallback = { result ->
                `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).convertToHub(group.id!!, result).subscribe({ successResult ->
                    group.name = result
                    `$`(StoreHandler::class.java).store.box(Group::class.java).put(group)
                    onGroupUpdateListener.invoke(group)
                }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
            }
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.set_name)
            show()
        }
    }

    fun setBackground(group: Group, onGroupUpdateListener: (Group) -> Unit) {
        `$`(DefaultMenus::class.java).uploadPhoto { photoId -> handlePhoto(group, photoId, onGroupUpdateListener) }
    }

    private fun handlePhoto(group: Group, photoId: String, onGroupUpdateListener: (Group) -> Unit) {
        val photo = `$`(PhotoUploadGroupMessageHandler::class.java).getPhotoPathFromId(photoId)
        `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).setGroupPhoto(group.id!!, photo).subscribe(
                { successResult ->
                    if (successResult.success) {
                        group.photo = photo
                        `$`(StoreHandler::class.java).store.box(Group::class.java).put(group)
                        onGroupUpdateListener.invoke(group)
                    } else {
                        `$`(DefaultAlerts::class.java).thatDidntWork()
                    }
                },
                { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }
        ))
    }

    fun setAbout(group: Group, about: String, onGroupUpdateListener: (Group) -> Unit) {
        `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).setGroupAbout(group.id!!, about).subscribe(
                { successResult ->
                    if (successResult.success) {
                        group.about = about
                        `$`(StoreHandler::class.java).store.box(Group::class.java).put(group)
                        onGroupUpdateListener.invoke(group)
                    } else {
                        `$`(DefaultAlerts::class.java).thatDidntWork()
                    }
                },
                { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }
        ))
    }
}
