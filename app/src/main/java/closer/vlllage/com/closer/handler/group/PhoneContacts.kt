package closer.vlllage.com.closer.handler.group

import android.provider.ContactsContract
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PhoneContacts constructor(private val on: On) {

    private var contacts: List<PhoneContact>? = null

    val allContacts: Observable<List<PhoneContact>>
        get() = if (contacts?.isEmpty() == false) {
            Observable.just(contacts!!)
        } else Observable.fromCallable<List<PhoneContact>> {
            val contentResolver = on<ApplicationHandler>().app.contentResolver ?: return@fromCallable listOf<PhoneContact>()

            contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)!!.use { cursor ->
                if (cursor.moveToFirst()) {
                    val contactsList = mutableListOf<PhoneContact>()

                    do {
                        val contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        val contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        contactsList.add(PhoneContact(contactName, contactNumber))
                    } while (cursor.moveToNext())

                    contactsList.sortWith { c1, c2 -> c1.name!!.compareTo(c2.name!!) }
                    this.contacts = contactsList
                    return@fromCallable contactsList
                }
            }

            listOf()
        }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

    fun forceReload() {
        contacts = null
    }
}
