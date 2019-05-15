package closer.vlllage.com.closer.handler.group

import com.queatz.on.On
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*

class PhoneNumberHandler constructor(private val on: On) {

    fun isValidNumber(phoneNumber: String): Boolean {
        return try {
            phoneNumberUtil.isValidNumber(phoneNumberUtil.parse(phoneNumber, Locale.US.country))
        } catch (e: NumberParseException) {
            e.printStackTrace()
            false
        }

    }

    fun normalize(phoneNumber: String): String? {
        return try {
            val number = phoneNumberUtil.parse(phoneNumber, Locale.US.country)
            phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    }
}
