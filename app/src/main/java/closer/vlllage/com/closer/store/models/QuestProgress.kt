package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class QuestProgress : BaseObject() {
    var questId: String? = null
    var groupId: String? = null
}
