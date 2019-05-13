package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember

class ListEqual : PoolMember() {
    fun isEqual(a: List<*>?, b: List<*>?): Boolean {
        if (a == null || b == null) {
            return a == null && b == null
        }

        if (a.size != b.size) {
            return false
        }

        for (i in a.indices) {
            val oA = a[i]
            val oB = a[i]
            if (oA == null && oB != null || oA != null && oA != oB) {
                return false
            }
        }

        return true
    }
}
