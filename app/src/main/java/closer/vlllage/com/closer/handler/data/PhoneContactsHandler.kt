package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.handler.group.PhoneContact
import closer.vlllage.com.closer.handler.group.PhoneContacts
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On
import com.queatz.on.OnLifecycle
import io.reactivex.Observable

class PhoneContactsHandler constructor(private val on: On) : OnLifecycle {

    private lateinit var phoneContacts: PhoneContacts

    val allContacts: Observable<List<PhoneContact>>
        get() = phoneContacts.allContacts

    override fun on() {
        phoneContacts = on<ApplicationHandler>().app.on<PhoneContacts>()
    }

    fun forceReload() {
        phoneContacts.forceReload()
    }
}
