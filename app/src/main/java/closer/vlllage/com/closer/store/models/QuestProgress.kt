package closer.vlllage.com.closer.store.models

import closer.vlllage.com.closer.store.QuestProgressFlowJsonConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity

@Entity
class QuestProgress : BaseObject() {
    var questId: String? = null
    var ofId: String? = null

    @Convert(converter = QuestProgressFlowJsonConverter::class, dbType = String::class)
    var progress: QuestProgressFlow? = null
}

class QuestProgressFlow {
    var items: List<QuestProgressAction> = listOf()
}

class QuestProgressAction {
    var groupActionId: String? = null
    var current: Int? = null
}
