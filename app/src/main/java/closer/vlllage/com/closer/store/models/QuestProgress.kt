package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class QuestProgress : BaseObject() {
    var questId: String? = null
    var groupId: String? = null
    var progress: String? = null
}

@Entity
class QuestProgressAction : BaseObject() {
    var questId: String? = null
    var groupActionId: String? = null
    var progress: Int? = null
}
