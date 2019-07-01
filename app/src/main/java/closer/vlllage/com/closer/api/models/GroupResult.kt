package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.Group
import com.google.gson.annotations.SerializedName
import java.lang.Boolean.TRUE

class GroupResult : ModelResult() {
    var name: String? = null
    var about: String? = null
    @SerializedName("public")
    var isPublic: Boolean? = null
    var physical: Boolean? = null
    var hub: Boolean? = null
    var eventId: String? = null
    var phoneId: String? = null
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
            group.isPublic = TRUE == groupResult.isPublic
            group.physical = TRUE == groupResult.physical
            group.hub = TRUE == groupResult.hub
            group.eventId = groupResult.eventId
            group.phoneId = groupResult.phoneId
            group.photo = groupResult.photo

            if (groupResult.geo != null && !groupResult.geo!!.isEmpty()) {
                group.latitude = groupResult.geo!![0]
                group.longitude = groupResult.geo!![1]
            }

            return group
        }
    }
}
