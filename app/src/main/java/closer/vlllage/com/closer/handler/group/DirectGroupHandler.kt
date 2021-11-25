package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupContact_
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On
import io.objectbox.rx3.RxQuery
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class DirectGroupHandler constructor(private val on: On) {
    fun getContactName(groupId: String): Single<String> {
        return getContactPhone(groupId).map {
            on<NameHandler>().getName(it)
        }
    }

    fun getContactPhone(groupId: String): Single<Phone> {
        return RxQuery.single(on<StoreHandler>().store.box(GroupContact::class).query(
                GroupContact_.groupId.equal(groupId).and(
                        GroupContact_.contactId.notEqual(on<PersistenceHandler>().phoneId!!)
                )
        )
                .build()).observeOn(AndroidSchedulers.mainThread()).flatMap {
            it.firstOrNull()?.let { groupContact ->
                on<DataHandler>().getPhone(groupContact.contactId!!)
            } ?: on<ApiHandler>().getContacts(groupId).flatMap {
                on<RefreshHandler>().handleGroupContacts(it, noGroups = true, removeAllExcept = false)

                it.firstOrNull { it.phone != null && it.phone?.id != on<PersistenceHandler>().phoneId!! }?.let {
                    on<DataHandler>().getPhone(it.phone!!.id!!)
                } ?: Single.error(Exception("Not found"))
            }
        }
    }
}