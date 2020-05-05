package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.Group
import com.google.gson.annotations.SerializedName

class GroupResult : ModelResult() {
    var name: String? = null
    var about: String? = null
    @SerializedName("public")
    var isPublic: Boolean? = null
    var physical: Boolean? = null
    var hub: Boolean? = null
    var eventId: String? = null
    var phoneId: String? = null
    var groupMessageId: String? = null
    var ratingAverage: Double? = null
    var ratingCount: Int? = null
    var geo: List<Double>? = null
    private val photo: String? = null

    companion object {

        fun from(groupResult: GroupResult): Group {
            val group = Group()
            group.id = groupResult.id
            updateFrom(group, groupResult)
            return group
        }

        fun updateFrom(group: Group, groupResult: GroupResult): Group {
            group.name = groupResult.name
            group.updated = groupResult.updated
            group.about = groupResult.about
            group.ratingAverage = groupResult.ratingAverage
            group.ratingCount = groupResult.ratingCount
            group.isPublic = true == groupResult.isPublic
            group.physical = true == groupResult.physical
            group.hub = true == groupResult.hub
            group.eventId = groupResult.eventId
            group.phoneId = groupResult.phoneId
            group.groupMessageId = groupResult.groupMessageId
            group.photo = groupResult.photo

            if (groupResult.geo.isNullOrEmpty().not()) {
                group.latitude = groupResult.geo!![0]
                group.longitude = groupResult.geo!![1]
            }

            return group
        }
    }
}
