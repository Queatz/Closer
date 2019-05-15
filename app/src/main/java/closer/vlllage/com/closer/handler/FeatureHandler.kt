package closer.vlllage.com.closer.handler

import com.queatz.on.On

class FeatureHandler constructor(private val on: On) {
    fun has(featureType: FeatureType): Boolean {
        return true
    }
}
