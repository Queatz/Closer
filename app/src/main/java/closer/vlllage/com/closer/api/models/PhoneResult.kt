package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.Phone

class PhoneResult : ModelResult() {
    var geo: List<Double>? = null
    var name: String? = null
    var photo: String? = null
    var status: String? = null
    var active: Boolean? = null
    var verified: Boolean? = null

    companion object {

        fun from(phoneResult: PhoneResult): Phone {
            return updateFrom(Phone(), phoneResult)
        }

        fun updateFrom(phone: Phone, phoneResult: PhoneResult): Phone {
            phone.id = phoneResult.id
            phone.updated = phoneResult.updated

            if (phoneResult.geo != null && phoneResult.geo!!.size == 2) {
                phone.latitude = phoneResult.geo!![0]
                phone.longitude = phoneResult.geo!![1]
            }

            phone.name = phoneResult.name
            phone.status = phoneResult.status
            phone.photo = phoneResult.photo
            phone.verified = phoneResult.verified

            return phone
        }
    }
}
