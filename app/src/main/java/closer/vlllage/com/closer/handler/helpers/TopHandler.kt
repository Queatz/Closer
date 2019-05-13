package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember

class TopHandler : PoolMember() {

    private var activeGroupId: String? = null

    fun isGroupActive(groupId: String): Boolean {
        return activeGroupId != null && activeGroupId == groupId
    }

    fun setGroupActive(groupId: String?) {
        activeGroupId = groupId
    }
}
