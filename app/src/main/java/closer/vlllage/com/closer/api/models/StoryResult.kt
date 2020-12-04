package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.Story

class StoryResult : ModelResult() {
    var text: String? = null
    var photo: String? = null
    var geo: List<Double>? = null
    var creator: String? = null

    var phone: PhoneResult? = null

    companion object {

        fun from(storyResult: StoryResult): Story {
            val story = Story()
            story.id = storyResult.id
            updateFrom(story, storyResult)
            return story
        }

        fun updateFrom(story: Story, storyResult: StoryResult): Story {
            story.text = storyResult.text
            story.photo = storyResult.photo
            story.creator = storyResult.creator
            story.latitude = storyResult.geo?.get(0)
            story.longitude = storyResult.geo?.get(1)
            return story
        }
    }
}
