package closer.vlllage.com.closer.handler.phone

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import closer.vlllage.com.closer.handler.helpers.Val
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupInvite
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import com.queatz.on.On
import java.util.*

class NameHandler constructor(private val on: On) {

    fun getName(phoneId: String): String {
        val phoneList = on<StoreHandler>().store.box(Phone::class).query().equal(Phone_.id, phoneId).build().find()
        return if (phoneList.isEmpty()) {
            on<ResourcesHandler>().resources.getString(R.string.unknown)
        } else getName(phoneList[0])
    }

    fun getName(phone: Phone?): String {
        if (phone == null) {
            return noName()
        }

        val name = if (on<Val>().isEmpty(phone.name))
            fallbackName(phone.id)
        else
            phone.name

        return if (isInactive(phone)) {
            on<ResourcesHandler>().resources.getString(R.string.contact_inactive_inline, name)
        } else {
            name!!
        }
    }

    fun getName(groupContact: GroupContact?): String {
        if (groupContact == null) {
            return noName()
        }

        val name = if (on<Val>().isEmpty(groupContact.contactName))
            fallbackName(groupContact.contactId)
        else
            groupContact.contactName

        return if (isInactive(groupContact)) {
            on<ResourcesHandler>().resources.getString(R.string.contact_inactive_inline, name)
        } else {
            name!!
        }
    }

    fun getName(groupInvite: GroupInvite?): String? {
        if (groupInvite == null) {
            return noName()
        }

        return if (on<Val>().isEmpty(groupInvite.name)) noName() else groupInvite.name
    }

    private fun fallbackName(phoneId: String?): String {
        if (phoneId == null) {
            return noName()
        }

        val index = Random(phoneId.hashCode().toLong()).nextInt(fallbackNames.size)
        return fallbackNames[index]
    }

    private fun noName(): String {
        return on<ResourcesHandler>().resources.getString(R.string.no_name)
    }

    private fun isInactive(groupContact: GroupContact): Boolean {
        return if (true) false else groupContact.contactActive!!.before(on<TimeAgo>().fifteenDaysAgo()) // XXX TODO Restore this after server IDs are fixed!
    }

    private fun isInactive(phone: Phone): Boolean {
        return if (true) false else phone.updated!!.before(on<TimeAgo>().fifteenDaysAgo()) // XXX TODO Restore this after server IDs are fixed!
    }

    companion object {

        private val fallbackNames = arrayOf("Random Armadillo", "Random Aardvark", "Random Kangaroo", "Random Gorilla", "Random Chimpanzee", "Random Anaconda", "Random Parakeet", "Random Rhino", "Random Muskrat", "Random Bumblebee", "Random Tiger", "Random Ocelot", "Random Capybara", "Random Sloth", "Random Lemur", "Random Baboon")
    }
}
