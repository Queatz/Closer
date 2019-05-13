package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity
import java.util.*

@Entity
class PhoneMessage : BaseObject() {
    var to: String? = null
    var from: String? = null
    var time: Date? = null
    var text: String? = null
    var attachment: String? = null
}