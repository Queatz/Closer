package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.Id
import java.util.*

@BaseEntity
open class BaseObject {
    @Id var objectBoxId: Long = 0
    var id: String? = null
    var localOnly: Boolean = false
    var updated: Date? = null
    var created: Date? = null
}
