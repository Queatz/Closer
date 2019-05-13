package closer.vlllage.com.closer.handler.group

class PhoneContact(var name: String?, var phoneNumber: String?) {
    var phoneId: String? = null

    val firstName: String
        get() {
            val firstWhiteSpace = name!!.indexOf(" ")

            return if (firstWhiteSpace == -1) {
                name!!
            } else name!!.substring(0, firstWhiteSpace)

        }
}
