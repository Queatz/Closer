package closer.vlllage.com.closer.api.models

class LifestyleResult : ModelResult() {
    var name: String? = null
    var phonesCount: Int? = null
    var phones: List<PhoneResult>? = null
}