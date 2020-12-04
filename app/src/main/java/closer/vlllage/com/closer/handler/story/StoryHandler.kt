package closer.vlllage.com.closer.handler.story

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.LocationHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.helpers.DefaultInput
import closer.vlllage.com.closer.handler.helpers.DefaultMenus
import closer.vlllage.com.closer.handler.helpers.ToastHandler
import closer.vlllage.com.closer.store.models.Story
import com.queatz.on.On

class StoryHandler(private val on: On) {
    fun addToStory() {
        on<LocationHandler>().getCurrentLocation { location ->
            on<DefaultMenus>().uploadPhoto { photoId ->
                val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)

                on<DefaultInput>().show(R.string.add_to_your_story, hintRes = R.string.write_here, buttonRes = R.string.post_story) { text ->
                    val story = Story()
                    story.latitude = location.latitude
                    story.longitude = location.longitude
                    story.text = text
                    story.photo = photo
                    story.creator = on<PersistenceHandler>().phoneId

                    on<SyncHandler>().sync(story) {
                        on<ToastHandler>().show(R.string.your_story_updated)
                    }
                }
            }
        }
    }
}