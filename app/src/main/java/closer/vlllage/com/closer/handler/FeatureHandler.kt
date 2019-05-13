package closer.vlllage.com.closer.handler

import closer.vlllage.com.closer.pool.PoolMember

class FeatureHandler : PoolMember() {
    fun has(featureType: FeatureType): Boolean {
        return true
    }
}
