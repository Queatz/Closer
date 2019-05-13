package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.media.MediaHandler
import closer.vlllage.com.closer.pool.PoolActivity
import closer.vlllage.com.closer.pool.PoolMember

class DefaultMenus : PoolMember() {
    fun uploadPhoto(onPhotoUploadedListener: (photoId: String) -> Unit) {
        `$`(MenuHandler::class.java).show(
                MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.take_photo) {
                    (`$`(ActivityHandler::class.java).activity as PoolActivity).pool
                            .`$`(CameraHandler::class.java)
                            .showCamera { photoUri -> `$`(PhotoUploadGroupMessageHandler::class.java).upload(photoUri!!, onPhotoUploadedListener) }
                },
                MenuHandler.MenuOption(R.drawable.ic_photo_black_24dp, R.string.upload_photo) {
                    (`$`(ActivityHandler::class.java).activity as PoolActivity).pool
                            .`$`(MediaHandler::class.java)
                            .getPhoto { photoUri -> `$`(PhotoUploadGroupMessageHandler::class.java).upload(photoUri, onPhotoUploadedListener) }
                })
    }
}
