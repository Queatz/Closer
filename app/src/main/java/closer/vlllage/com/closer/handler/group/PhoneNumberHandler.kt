package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.pool.PoolMember
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*

class PhoneNumberHandler : PoolMember() {

    fun isValidNumber(phoneNumber: String): Boolean {
        try {
            return phoneNumberUtil.isValidNumber(phoneNumberUtil.parse(phoneNumber, Locale.US.country))
        } catch (e: NumberParseException) {
            e.printStackTrace()
            return false
        }

    }

    fun normalize(phoneNumber: String): String? {
        try {
            val number = phoneNumberUtil.parse(phoneNumber, Locale.US.country)
            return phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (e: NumberParseException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {

        private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    }
}
