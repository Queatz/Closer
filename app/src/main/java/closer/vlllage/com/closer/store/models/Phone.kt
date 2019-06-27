package closer.vlllage.com.closer.store.models

import closer.vlllage.com.closer.store.StringListJsonConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity

@Entity
class Phone : BaseObject() {
    var name: String? = null
    var status: String? = null
    var introduction: String? = null
    var offtime: String? = null
    var history: String? = null
    var photo: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var verified: Boolean? = null

    @Convert(converter = StringListJsonConverter::class, dbType = String::class)
    var goals: List<String>? = null

    @Convert(converter = StringListJsonConverter::class, dbType = String::class)
    var lifestyles: List<String>? = null
}
