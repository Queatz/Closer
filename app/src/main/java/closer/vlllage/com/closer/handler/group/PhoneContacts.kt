package closer.vlllage.com.closer.handler.group

import android.provider.ContactsContract
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.pool.PoolMember
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class PhoneContacts : PoolMember() {

    private var contacts: List<PhoneContact>? = null

    val allContacts: Observable<List<PhoneContact>>
        get() = if (contacts != null && !contacts!!.isEmpty()) {
            Observable.just(contacts!!)
        } else Observable.fromCallable<List<PhoneContact>> {
            val contentResolver = `$`(ApplicationHandler::class.java).app.contentResolver

            if (contentResolver == null) {
                return@fromCallable listOf<PhoneContact>()
            }

            contentResolver!!.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)!!.use { cursor ->
                if (cursor == null) {
                    return@fromCallable listOf<PhoneContact>()
                }

                if (cursor.moveToFirst()) {
                    val contactsList = ArrayList<PhoneContact>()

                    do {
                        val contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        val contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        contactsList.add(PhoneContact(contactName, contactNumber))
                    } while (cursor.moveToNext())

                    contactsList.sortWith(Comparator { c1, c2 -> c1.name!!.compareTo(c2.name!!) })
                    this.contacts = contactsList
                    return@fromCallable contactsList
                }
            }

            ArrayList()
        }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

    fun forceReload() {
        contacts = null
    }
}
