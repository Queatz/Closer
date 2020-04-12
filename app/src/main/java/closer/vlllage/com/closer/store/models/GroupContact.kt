package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity
import java.util.*

@Entity
class GroupContact : BaseObject() {
    var groupId: String? = null
    var contactId: String? = null
    var contactName: String? = null
    var contactActive: Date? = null
    var status: String? = null
    var photo: String? = null
}
