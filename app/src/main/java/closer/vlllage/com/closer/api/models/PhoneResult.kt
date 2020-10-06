package closer.vlllage.com.closer.api.models

class PhoneResult : ModelResult() {
    var geo: List<Double>? = null
    var geoIsApprox: Boolean? = null
    var name: String? = null
    var photo: String? = null
    var status: String? = null
    var introduction: String? = null
    var offtime: String? = null
    var occupation: String? = null
    var history: String? = null
    var active: Boolean? = null
    var verified: Boolean? = null
    var goals: List<GoalResult>? = null
    var lifestyles: List<LifestyleResult>? = null
}
