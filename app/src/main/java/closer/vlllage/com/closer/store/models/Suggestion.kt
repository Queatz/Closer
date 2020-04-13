package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity
import java.util.*

@Entity
class Suggestion : BaseObject() {
    var name: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
}
