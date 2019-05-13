package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class Pin : BaseObject() {
    var from: String? = null
    var to: String? = null
}
