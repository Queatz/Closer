package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.Quest
import closer.vlllage.com.closer.store.models.QuestFlow
import com.google.gson.annotations.SerializedName

class QuestResult : ModelResult() {
    var name: String? = null
    var groupId: String? = null
    @SerializedName("public")
    var isPublic: Boolean = false
    var geo: List<Double>? = null
    var flow: QuestFlow? = null

    companion object {

        fun from(questResult: QuestResult): Quest {
            val quest = Quest()
            quest.id = questResult.id
            updateFrom(quest, questResult)
            return quest
        }

        fun updateFrom(quest: Quest, questResult: QuestResult): Quest {
            quest.name = questResult.name
            quest.flow = questResult.flow
            quest.isPublic = questResult.isPublic
            quest.latitude = questResult.geo!![0]
            quest.longitude = questResult.geo!![1]
            quest.groupId = questResult.groupId
            return quest
        }
    }
}
