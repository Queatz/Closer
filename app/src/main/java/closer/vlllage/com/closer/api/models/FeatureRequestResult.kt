package closer.vlllage.com.closer.api.models

class FeatureRequestResult : ModelResult() {
    var name: String? = null
    var description: String? = null
    var votes: Int = 0
    var voted: Boolean = false
}
