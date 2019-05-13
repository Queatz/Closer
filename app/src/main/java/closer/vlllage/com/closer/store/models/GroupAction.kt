package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class GroupAction : BaseObject() {
    var group: String? = null
    var name: String? = null
    var intent: String? = null
    var photo: String? = null
}
