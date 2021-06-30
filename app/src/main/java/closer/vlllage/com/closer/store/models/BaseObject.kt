package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.ConflictStrategy
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import java.util.*

@BaseEntity
open class BaseObject {
    @Id var objectBoxId: Long = 0
    @Unique(onConflict = ConflictStrategy.REPLACE) var id: String? = null
    var localOnly: Boolean = false
    var updated: Date? = null
    var created: Date? = null
}
