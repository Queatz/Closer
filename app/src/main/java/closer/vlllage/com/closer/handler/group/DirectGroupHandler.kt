package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupContact_
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On
import io.objectbox.rx.RxQuery
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class DirectGroupHandler constructor(private val on: On) {
    fun getContactName(groupId: String): Single<String> {
        return getContactPhone(groupId).map {
            it.name
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
            } ?: Single.error(Exception("Not found"))
        }
    }
}