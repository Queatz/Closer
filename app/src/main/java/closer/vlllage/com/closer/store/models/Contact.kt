package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class Contact : BaseObject() {
    var name: String? = null
    var phoneNumber: String? = null
    var closerAccountId: String? = null
}
