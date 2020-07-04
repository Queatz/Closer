package closer.vlllage.com.closer.store.models

import closer.vlllage.com.closer.store.QuestProgressFlowJsonConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Uid
import java.util.*

@Entity
class QuestProgress : BaseObject() {
    var questId: String? = null
    var ofId: String? = null
    val groupId: String? = null
    var finished: Date? = null
    var stopped: Date? = null
    var active: Boolean? = null

    @Convert(converter = QuestProgressFlowJsonConverter::class, dbType = String::class)
    var progress: QuestProgressFlow? = QuestProgressFlow()
}

class QuestProgressFlow {
    var items: MutableMap<String, QuestProgressAction> = mutableMapOf()
}

class QuestProgressAction {
    var groupActionId: String? = null
    var current: Int? = null
}
