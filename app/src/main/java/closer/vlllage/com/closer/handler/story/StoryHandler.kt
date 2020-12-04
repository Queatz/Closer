package closer.vlllage.com.closer.handler.story

import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.helpers.DefaultMenus
import com.queatz.on.On

class StoryHandler(private val on: On) {
    fun addToStory() {
        on<DefaultMenus>().uploadPhoto { photoId ->
            val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)
        }
    }
}