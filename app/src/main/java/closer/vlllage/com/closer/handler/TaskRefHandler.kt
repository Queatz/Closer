package closer.vlllage.com.closer.handler

import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On

class TaskRefHandler constructor(private val on: On) {
    var activeTask: TaskDefinition? = null
}

enum class TaskType {
    CREATE_EVENT_IN_GROUP
}

data class TaskDefinition constructor(
    val taskType: TaskType,
    val group: Group
)