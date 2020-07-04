package closer.vlllage.com.closer.store.models

import closer.vlllage.com.closer.store.QuestFlowJsonConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import java.util.*

@Entity
class Quest : BaseObject() {
    var name: String? = null
    var groupId: String? = null
    var isPublic: Boolean = false
    var latitude: Double? = null
    var longitude: Double? = null

    @Convert(converter = QuestFlowJsonConverter::class, dbType = String::class)
    var flow: QuestFlow? = null
}

data class QuestFlow constructor(
        var finish: QuestFinish? = null,
        var items: List<QuestAction> = listOf()
)

data class QuestAction constructor(
        var groupActionId: String? = null,
        var type: QuestActionType = QuestActionType.Repeat,
        var value: Int = 1,
        var current: Int = 0
)

data class QuestFinish constructor(
        var date: Date? = null,
        var duration: Int? = null,
        var unit: QuestDurationUnit? = null
)

enum class QuestActionType {
    Percent,
    Repeat
}

enum class QuestDurationUnit {
    Day,
    Week,
    Month
}
