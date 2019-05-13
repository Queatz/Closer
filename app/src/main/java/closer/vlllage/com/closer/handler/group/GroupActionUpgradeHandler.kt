package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.CameraHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.media.MediaHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupAction

class GroupActionUpgradeHandler : PoolMember() {

    fun setPhotoFromMedia(groupAction: GroupAction) {
        `$`(MediaHandler::class.java).getPhoto { photoUri ->
            `$`(PhotoUploadGroupMessageHandler::class.java).upload(photoUri) { photoId ->
                val photo = `$`(PhotoUploadGroupMessageHandler::class.java).getPhotoPathFromId(photoId)
                `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java)
                        .setGroupActionPhoto(groupAction.id!!, photo).subscribe(
                                { successResult ->
                                    if (successResult.success) {
                                        groupAction.photo = photo
                                        `$`(StoreHandler::class.java).store.box(GroupAction::class.java).put(groupAction)
                                    } else {
                                        `$`(DefaultAlerts::class.java).thatDidntWork()
                                    }
                                },
                                { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }
                        ))
            }
        }
    }

    fun setPhotoFromCamera(groupAction: GroupAction) {
        `$`(CameraHandler::class.java).showCamera { photoUri ->
            `$`(PhotoUploadGroupMessageHandler::class.java).upload(photoUri!!) { photoId ->
                val photo = `$`(PhotoUploadGroupMessageHandler::class.java).getPhotoPathFromId(photoId)
                `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java)
                        .setGroupActionPhoto(groupAction.id!!, photo).subscribe(
                                { successResult ->
                                    if (successResult.success) {
                                        groupAction.photo = photo
                                        `$`(StoreHandler::class.java).store.box(GroupAction::class.java).put(groupAction)
                                    } else {
                                        `$`(DefaultAlerts::class.java).thatDidntWork()
                                    }
                                },
                                { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }
                        ))
            }
        }
    }
}
