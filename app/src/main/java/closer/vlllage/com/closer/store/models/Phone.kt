package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class Phone : BaseObject() {
    var name: String? = null
    var status: String? = null
    var photo: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
}
