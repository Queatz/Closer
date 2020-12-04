package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Transient

@Entity
class Story : BaseObject() {
    var text: String? = null
    var photo: String? = null
    var creator: String? = null
    var latitude: Double? = null
    var longitude: Double? = null

    @Transient
    var phone: Phone? = null
}