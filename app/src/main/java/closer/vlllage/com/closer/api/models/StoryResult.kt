package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.handler.data.ApiModelHandler
import closer.vlllage.com.closer.store.models.Story
import com.queatz.on.On

class StoryResult : ModelResult() {
    var text: String? = null
    var photo: String? = null
    var geo: List<Double>? = null
    var creator: String? = null

    var phone: PhoneResult? = null

    companion object {

        fun from(on: On, storyResult: StoryResult): Story {
            val story = Story()
            story.id = storyResult.id
            updateFrom(on, story, storyResult)
            return story
        }

        fun updateFrom(on: On, story: Story, storyResult: StoryResult): Story {
            story.text = storyResult.text
            story.photo = storyResult.photo
            story.creator = storyResult.creator
            story.created = storyResult.created
            story.updated = storyResult.updated
            story.latitude = storyResult.geo?.get(0)
            story.longitude = storyResult.geo?.get(1)
            story.phone = storyResult.phone?.let { on<ApiModelHandler>().from(it) }
            return story
        }
    }
}
