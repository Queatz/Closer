package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class Group : BaseObject() {
    var name: String? = null
    var about: String? = null
    var isPublic: Boolean = false
    var hub: Boolean = false
    var physical: Boolean = false
    var eventId: String? = null
    var phoneId: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var photo: String? = null

    fun hasEvent() = eventId != null

    fun hasPhone() = phoneId != null
}
