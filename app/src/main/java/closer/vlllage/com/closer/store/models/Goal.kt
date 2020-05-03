package closer.vlllage.com.closer.store.models

import io.objectbox.annotation.Entity

@Entity
class Goal : BaseObject() {
    var name: String? = null
    var phonesCount: Int? = null
}
