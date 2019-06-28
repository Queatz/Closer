package closer.vlllage.com.closer.handler

import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On

class TaskHandler constructor(private val on: On) {
    var activeTask: TaskDefinition?
        get() = on<ApplicationHandler>().app.on<TaskRefHandler>().activeTask
        set(value) {
            on<ApplicationHandler>().app.on<TaskRefHandler>().activeTask = value
        }

}