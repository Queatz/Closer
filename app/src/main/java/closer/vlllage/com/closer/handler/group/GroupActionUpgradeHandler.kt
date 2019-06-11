package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.CameraHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.media.MediaHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupAction
import com.queatz.on.On

class GroupActionUpgradeHandler constructor(private val on: On) {

    fun setPhotoFromMedia(groupAction: GroupAction) {
        on<MediaHandler>().getPhoto { photoUri ->
            on<PhotoUploadGroupMessageHandler>().upload(photoUri) { photoId ->
                val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)
                on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                        .setGroupActionPhoto(groupAction.id!!, photo).subscribe(
                                { successResult ->
                                    if (successResult.success) {
                                        groupAction.photo = photo
                                        on<StoreHandler>().store.box(GroupAction::class).put(groupAction)
                                    } else {
                                        on<DefaultAlerts>().thatDidntWork()
                                    }
                                },
                                { on<DefaultAlerts>().thatDidntWork() }
                        ))
            }
        }
    }

    fun setPhotoFromCamera(groupAction: GroupAction) {
        on<CameraHandler>().showCamera { photoUri ->
            on<PhotoUploadGroupMessageHandler>().upload(photoUri!!) { photoId ->
                val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)
                on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                        .setGroupActionPhoto(groupAction.id!!, photo).subscribe(
                                { successResult ->
                                    if (successResult.success) {
                                        groupAction.photo = photo
                                        on<StoreHandler>().store.box(GroupAction::class).put(groupAction)
                                    } else {
                                        on<DefaultAlerts>().thatDidntWork()
                                    }
                                },
                                { on<DefaultAlerts>().thatDidntWork() }
                        ))
            }
        }
    }
}
