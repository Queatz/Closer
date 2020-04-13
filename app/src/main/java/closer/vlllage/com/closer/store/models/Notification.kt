package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity


@Entity
class Notification : BaseObject() {
    var name: String? = null
    var message: String? = null
    var intentAction: String? = null
    var intentTarget: String? = null
    var intentBundle: String? = null
}