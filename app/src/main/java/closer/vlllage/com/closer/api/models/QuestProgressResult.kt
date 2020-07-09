package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.QuestProgressFlow
import com.google.gson.annotations.SerializedName
import java.util.*

class QuestProgressResult : ModelResult() {
    var questId: String? = null
    var ofId: String? = null
    @SerializedName("public")
    var isPublic: Boolean = false
    val groupId: String? = null
    var finished: Date? = null
    var stopped: Date? = null
    var active: Boolean? = null
    var progress: QuestProgressFlow? = QuestProgressFlow()
}
