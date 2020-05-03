package closer.vlllage.com.closer.api.models

class GoalResult : ModelResult() {
    var name: String? = null
    var phonesCount: Int? = null
    var phones: List<PhoneResult>? = null
}