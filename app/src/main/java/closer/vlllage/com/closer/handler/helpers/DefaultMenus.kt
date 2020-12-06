package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.media.MediaHandler
import closer.vlllage.com.closer.pool.PoolActivity
import com.queatz.on.On

class DefaultMenus constructor(private val on: On) {
    fun uploadPhoto(notify: Boolean = false, onPhotoUploadedListener: (photoId: String) -> Unit) {
        on<MenuHandler>().show(
                MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.take_photo) {
                    (on<ActivityHandler>().activity as PoolActivity).on<CameraHandler>()
                            .showCamera { photoUri ->
                                if (notify) {
                                    on<ToastHandler>().show(R.string.uploading_photo)
                                }
                                on<PhotoUploadGroupMessageHandler>().upload(photoUri!!, onPhotoUploadedListener)
                            }
                },
                MenuHandler.MenuOption(R.drawable.ic_photo_black_24dp, R.string.upload_photo) {
                    (on<ActivityHandler>().activity as PoolActivity).on<MediaHandler>()
                            .getPhoto { photoUri ->
                                if (notify) {
                                    on<ToastHandler>().show(R.string.uploading_photo)
                                }
                                on<PhotoUploadGroupMessageHandler>().upload(photoUri, onPhotoUploadedListener)
                            }
                },
                button = "")
    }
}
