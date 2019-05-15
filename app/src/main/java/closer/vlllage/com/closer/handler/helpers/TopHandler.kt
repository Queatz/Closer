package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On

class TopHandler constructor(private val on: On) {

    private var activeGroupId: String? = null

    fun isGroupActive(groupId: String): Boolean {
        return activeGroupId != null && activeGroupId == groupId
    }

    fun setGroupActive(groupId: String?) {
        activeGroupId = groupId
    }
}
