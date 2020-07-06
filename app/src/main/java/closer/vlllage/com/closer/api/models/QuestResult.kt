package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.QuestFlow
import com.google.gson.annotations.SerializedName

class QuestResult : ModelResult() {
    var name: String? = null
    var groupId: String? = null
    @SerializedName("public")
    var isPublic: Boolean = false
    var geo: List<Double>? = null
    var flow: QuestFlow? = null
}
