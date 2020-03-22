package closer.vlllage.com.closer.store.models

import closer.vlllage.com.closer.store.ReactionCountListJsonConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import java.util.*

@Entity
class GroupMessage : BaseObject() {
    var to: String? = null
    var from: String? = null
    var time: Date? = null
    var text: String? = null
    var attachment: String? = null
    var latitude: Double? = null
    var longitude: Double? = null

    @Convert(converter = ReactionCountListJsonConverter::class, dbType = String::class)
    var reactions: List<ReactionCount> = listOf()
}
