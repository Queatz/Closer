package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.GroupResult
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class GroupHandler constructor(private val on: On) {

    private val connectionError = { _: Throwable -> on<ConnectionErrorHandler>().notifyConnectionError() }

    var group: Group? = null
        set(group) {
            field = group

            if (groupDataSubscription != null) {
                on<DisposableHandler>().dispose(groupDataSubscription!!)
            }

            if (group != null) {
                onGroupSet(group)
                groupChanged.onNext(group)
                setEventById(group.eventId)
                setPhoneById(group.phoneId)
                on<RefreshHandler>().refreshGroupMessages(group.id!!)
                on<RefreshHandler>().refreshGroupContacts(group.id!!)

                groupDataSubscription = on<StoreHandler>().store.box(Group::class.java).query()
                        .equal(Group_.id, group.id!!)
                        .build()
                        .subscribe()
                        .onlyChanges()
                        .on(AndroidScheduler.mainThread())
                        .observer { groups ->
                            if (groups.isEmpty()) return@observer
                            groupUpdated.onNext(groups[0])
                            on<RefreshHandler>().refreshGroupContacts(group.id!!)
                        }

                on<DisposableHandler>().add(groupDataSubscription!!)
            }
        }
    var event: Event? = null
        get() = eventChanged.value
        private set

    var groupContact: GroupContact? = null
        private set
    private val groupChanged = BehaviorSubject.create<Group>()
    private val groupUpdated = PublishSubject.create<Group>()
    private val eventChanged = BehaviorSubject.create<Event>()
    private val phoneChanged = BehaviorSubject.create<Phone>()
    private val contactInfoChanged = BehaviorSubject.create<ContactInfo>()
    private val contactInfo = ContactInfo()
    private var groupDataSubscription: DataSubscription? = null

    fun setGroupById(groupId: String?) {
        if (groupId == null) {
            group = null
            return
        }

        group = on<StoreHandler>().store.box(Group::class.java).query()
                .equal(Group_.id, groupId)
                .build().findFirst()

        if (group == null) {
            on<DisposableHandler>().add(on<ApiHandler>().getGroup(groupId)
                    .map { GroupResult.from(it) }
                    .subscribe { group ->
                        on<RefreshHandler>().refresh(group)
                        this.group = group
                    })
        }
    }

    private fun onGroupSet(group: Group) {
        setGroupContact()

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupContact::class.java)
                .query()
                .equal(GroupContact_.groupId, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupContacts ->
                    on<GroupContactsHandler>().setCurrentGroupContacts(groupContacts)
                    contactInfo.contactNames.clear()
                    for (groupContact in groupContacts) {
                        contactInfo.contactNames.add(on<NameHandler>().getName(groupContact))
                    }

                    contactInfoChanged.onNext(contactInfo)
                })

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupInvite::class.java)
                .query()
                .equal(GroupInvite_.group, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupInvites ->
                    contactInfo.contactInvites.clear()
                    for (groupInvite in groupInvites) {
                        contactInfo.contactInvites.add(on<ResourcesHandler>().resources.getString(R.string.contact_invited_inline, on<NameHandler>().getName(groupInvite)))
                    }
                    contactInfoChanged.onNext(contactInfo)
                })
    }

    private fun setGroupContact() {
        if (group == null || on<PersistenceHandler>().phoneId == null) {
            return
        }

        groupContact = on<StoreHandler>().store.box(GroupContact::class.java).query()
                .equal(GroupContact_.groupId, group!!.id!!)
                .equal(GroupContact_.contactId, on<PersistenceHandler>().phoneId!!)
                .build().findFirst()
    }

    private fun setEventById(eventId: String?) {
        if (eventId == null) {
            return
        }

        on<DisposableHandler>().add(on<DataHandler>().getEventById(eventId)
                .subscribe({ event -> eventChanged.onNext(event) },
                        { on<DefaultAlerts>().thatDidntWork() }))
    }

    private fun setPhoneById(phoneId: String?) {
        if (phoneId == null) {
            return
        }

        on<DisposableHandler>().add(on<DataHandler>().getPhone(phoneId)
                .subscribe({ phone -> phoneChanged.onNext(phone) },
                        { on<DefaultAlerts>().thatDidntWork() }))
    }

    fun onGroupChanged(callback: (Group) -> Unit) = onChangeCallback(groupChanged, callback)
    fun onGroupUpdated(callback: (Group) -> Unit) = onChangeCallback(groupUpdated, callback)
    fun onEventChanged(callback: (Event) -> Unit) = onChangeCallback(eventChanged, callback)
    fun onPhoneChanged(callback: (Phone) -> Unit) = onChangeCallback(phoneChanged, callback)
    fun onContactInfoChanged(callback: (ContactInfo) -> Unit) = onChangeCallback(contactInfoChanged, callback)

    private fun <T> onChangeCallback(observable: Observable<T>, callback: (T) -> Unit) {
        on<DisposableHandler>().add(observable.subscribe({
            callback.invoke(it)
        }, connectionError))
    }
}

class ContactInfo {
    val contactNames = mutableListOf<String>()
    val contactInvites = mutableListOf<String>()
}