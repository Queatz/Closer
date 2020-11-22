package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class Hide : BaseObject() {
    var groupId: String? = null
    var phoneId: String? = null
    var hide = true
}
