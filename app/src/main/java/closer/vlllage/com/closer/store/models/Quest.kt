package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class Quest : BaseObject() {
    var name: String? = null
    var groupId: String? = null
}
