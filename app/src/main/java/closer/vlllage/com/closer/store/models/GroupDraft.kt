package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class GroupDraft {
    @Id var objectBoxId: Long = 0
    var groupId: String? = null
    var message: String? = null
}
