package closer.vlllage.com.closer.store.models

import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Entity

@Entity
class Quest : BaseObject() {
    var name: String? = null
    var groupId: String? = null
    var flow: String? = null
//    @SerializedName("public")
//    private val isPublic: Boolean = false
}
