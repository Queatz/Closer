package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity
import java.util.*

@Entity
class GroupAction : BaseObject() {
    var group: String? = null
    var name: String? = null
    var intent: String? = null
    var about: String? = null
    var photo: String? = null
    var flow: String? = null
    var used: Date? = null
}
