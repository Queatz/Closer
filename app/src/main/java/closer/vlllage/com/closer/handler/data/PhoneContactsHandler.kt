package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.handler.group.PhoneContact
import closer.vlllage.com.closer.handler.group.PhoneContacts
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.pool.PoolMember
import io.reactivex.Observable

class PhoneContactsHandler : PoolMember() {

    private var phoneContacts: PhoneContacts? = null

    val allContacts: Observable<List<PhoneContact>>
        get() = phoneContacts!!.allContacts

    override fun onPoolInit() {
        phoneContacts = `$`(ApplicationHandler::class.java).app.`$`(PhoneContacts::class.java)
    }

    fun forceReload() {
        phoneContacts!!.forceReload()
    }
}
