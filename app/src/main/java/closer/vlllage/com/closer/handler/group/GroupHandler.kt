package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.R
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
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class GroupHandler constructor(private val on: On) {

    private val connectionError = { _: Throwable -> on<ConnectionErrorHandler>().notifyConnectionError() }

    private val disposableGroup = on<DisposableHandler>().group()

    var group: Group? = null
        set(group) {
            field = group

            disposableGroup.clear()

            if (group != null) {
                onGroupSet(group)
                groupChanged.onNext(group)
                setEventById(group.eventId)
                setPhoneById(group.phoneId)
                on<RefreshHandler>().refreshGroupMessages(group.id!!)
                on<RefreshHandler>().refreshGroupContacts(group.id!!)

                disposableGroup.add(on<StoreHandler>().store.box(Group::class.java).query()
                        .equal(Group_.id, group.id!!)
                        .build()
                        .subscribe()
                        .onlyChanges()
                        .on(AndroidScheduler.mainThread())
                        .observer { groups ->
                            if (groups.isEmpty()) return@observer
                            groupUpdated.onNext(groups[0])
                            on<RefreshHandler>().refreshGroupContacts(group.id!!)
                        })


                if (on<PersistenceHandler>().phoneId != null) {
                    disposableGroup.add(on<StoreHandler>().store.box(GroupMember::class.java).query()
                            .equal(GroupMember_.group, group.id!!)
                            .equal(GroupMember_.phone, on<PersistenceHandler>().phoneId!!)
                            .build()
                            .subscribe()
                            .on(AndroidScheduler.mainThread())
                            .observer { groupMembers ->
                                groupMemberChanged.onNext(if (groupMembers.isEmpty()) GroupMember() else groupMembers[0])
                            })
                }
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
    private val groupMemberChanged = BehaviorSubject.create<GroupMember>()
    private val contactInfo = ContactInfo()

    fun setGroupById(groupId: String?) {
        if (groupId == null) {
            group = null
            return
        }

        disposableGroup.add(on<DataHandler>().getGroupById(groupId).subscribe({
            group = it
        }, { on<DefaultAlerts>().thatDidntWork() }))
    }

    private fun onGroupSet(group: Group) {
        disposableGroup.add(on<StoreHandler>().store.box(GroupContact::class.java)
                .query()
                .equal(GroupContact_.groupId, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupContacts ->
                    groupContact = groupContacts.firstOrNull { it.contactId == on<PersistenceHandler>().phoneId!! }

                    on<GroupContactsHandler>().setCurrentGroupContacts(groupContacts)
                    contactInfo.contactNames.clear()
                    for (groupContact in groupContacts) {
                        contactInfo.contactNames.add(on<NameHandler>().getName(groupContact))
                    }

                    contactInfoChanged.onNext(contactInfo)
                })

        disposableGroup.add(on<StoreHandler>().store.box(GroupInvite::class.java)
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

    private fun setEventById(eventId: String?) {
        if (eventId == null) {
            return
        }

        disposableGroup.add(on<DataHandler>().getEventById(eventId)
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
    fun onGroupMemberChanged(callback: (GroupMember) -> Unit) = onChangeCallback(groupMemberChanged, callback)

    private fun <T> onChangeCallback(observable: Observable<T>, callback: (T) -> Unit) {
        on<DisposableHandler>().add(observable.subscribe({ callback.invoke(it) }, connectionError))
    }
}

class ContactInfo {
    val contactNames = mutableListOf<String>()
    val contactInvites = mutableListOf<String>()
}