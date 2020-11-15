package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.QuestProgress
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

    companion object {

        fun from(questProgressResult: QuestProgressResult): QuestProgress {
            val questProgress = QuestProgress()
            questProgress.id = questProgressResult.id
            updateFrom(questProgress, questProgressResult)
            return questProgress
        }

        fun updateFrom(questProgress: QuestProgress, questProgressResult: QuestProgressResult): QuestProgress {
            questProgress.progress = questProgressResult.progress
            questProgress.isPublic = questProgressResult.isPublic
            questProgress.ofId = questProgressResult.ofId
            questProgress.questId = questProgressResult.questId
            questProgress.groupId = questProgressResult.groupId
            questProgress.finished = questProgressResult.finished
            questProgress.stopped = questProgressResult.stopped
            questProgress.created = questProgressResult.created
            questProgress.updated = questProgressResult.updated
            questProgress.active = questProgressResult.active ?: false
            return questProgress
        }
    }
}
