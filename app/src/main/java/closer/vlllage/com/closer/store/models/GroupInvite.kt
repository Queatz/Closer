package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class GroupInvite : BaseObject() {
    var group: String? = null
    var name: String? = null
}
