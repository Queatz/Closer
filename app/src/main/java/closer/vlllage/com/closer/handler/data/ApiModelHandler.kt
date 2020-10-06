package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.api.models.GoalResult
import closer.vlllage.com.closer.api.models.LifestyleResult
import closer.vlllage.com.closer.api.models.PhoneResult
import closer.vlllage.com.closer.handler.phone.NameCacheHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.store.models.Goal
import closer.vlllage.com.closer.store.models.Lifestyle
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On

class ApiModelHandler constructor(private val on: On) {

    fun from(phoneResult: PhoneResult): Phone {
        return updateFrom(Phone(), phoneResult)
    }

    fun updateFrom(phone: Phone, phoneResult: PhoneResult): Phone {
        phone.id = phoneResult.id
        phone.updated = phoneResult.updated
        phone.created = phoneResult.created

        phone.geoIsApprox = phoneResult.geoIsApprox
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

        on<RefreshHandler>().handleLifestylesAndGoals(phoneResult)

        if (phone.id != null) {
            on<NameCacheHandler>()[phone.id!!] = on<NameHandler>().getName(phone)
        }

        return phone
    }

    fun from(goalResult: GoalResult): Goal {
        return updateFrom(Goal(), goalResult)
    }

    fun updateFrom(goal: Goal, goalResult: GoalResult): Goal {
        goal.id = goalResult.id
        goalResult.name?.let { goal.name = it }
        goalResult.phonesCount?.let { goal.phonesCount = it }
        return goal
    }

    fun from(lifestyleResult: LifestyleResult): Lifestyle {
        return updateFrom(Lifestyle(), lifestyleResult)
    }

    fun updateFrom(lifestyle: Lifestyle, lifestyleResult: LifestyleResult): Lifestyle {
        lifestyle.id = lifestyleResult.id
        lifestyleResult.name?.let { lifestyle.name = it }
        lifestyleResult.phonesCount?.let { lifestyle.phonesCount = it }
        return lifestyle
    }
}