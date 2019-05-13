package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class GroupMember : BaseObject() {
    var phone: String? = null
    var group: String? = null
    var muted: Boolean = false
    var subscribed: Boolean = false
}
