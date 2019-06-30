package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.Phone

class PhoneResult : ModelResult() {
    var geo: List<Double>? = null
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
            phone.introduction = phoneResult.introduction
            phone.offtime = phoneResult.offtime
            phone.occupation = phoneResult.occupation
            phone.history = phoneResult.history
            phone.photo = phoneResult.photo
            phone.verified = phoneResult.verified
            phone.goals = phoneResult.goals?.map { it.name ?: "-" }
            phone.lifestyles = phoneResult.lifestyles?.map { it.name ?: "-" }

            return phone
        }
    }
}
