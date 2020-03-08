package closer.vlllage.com.closer.api.models

class InviteCodeResult : ModelResult() {
    var group: String? = null
    var name: String? = null
    var code: String? = null
    var phone: PhoneResult? = null
    var used = false
}
