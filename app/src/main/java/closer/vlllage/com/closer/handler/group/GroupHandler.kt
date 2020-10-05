package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class GroupHandler constructor(private val on: On) {

    private val connectionError = { throwable: Throwable ->
        throwable.printStackTrace()
        on<ConnectionErrorHandler>().notifyConnectionError()
    }

    private val disposableGroup = on<DisposableHandler>().group()

    var group: Group? = null
        set(group) {
            if (group?.id == field?.id) {
                return
            }

            field = group
            disposableGroup.clear()

            if (group == null) {
                return
            }

            onGroupSet(group)
            groupChanged.onNext(group)
            setEventById(group.eventId)
            setPhoneById(group.phoneId)
            on<RefreshHandler>().refreshGroupMessages(group.id!!)
            on<RefreshHandler>().refreshGroupContacts(group.id!!)

            disposableGroup.add(on<StoreHandler>().store.box(Group::class).query()
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

            on<PersistenceHandler>().phoneId?.let { phoneId ->
                disposableGroup.add(on<StoreHandler>().store.box(GroupMember::class).query()
                        .equal(GroupMember_.group, group.id!!)
                        .equal(GroupMember_.phone, phoneId)
                        .build()
                        .subscribe()
                        .on(AndroidScheduler.mainThread())
                        .observer { groupMembers ->
                            groupMemberChanged.onNext(if (groupMembers.isEmpty()) GroupMember() else groupMembers[0])
                        })
            }
        }

    var event: Event? = null
        get() = eventChanged.value
        private set

    var phone: Phone? = null
        get() = phoneChanged.value
        private set

    var groupContact: GroupContact? = null
        private set

    private val groupChanged = BehaviorSubject.create<Group>()
    private val groupUpdated = PublishSubject.create<Group>()
    private val groupNotFound = PublishSubject.create<Unit>()
    private val eventChanged = BehaviorSubject.create<Event>()
    private val phoneChanged = BehaviorSubject.create<Phone>()
    private val phoneUpdated = BehaviorSubject.create<Phone>()
    private val contactInfoChanged = BehaviorSubject.create<ContactInfo>()
    private val groupMemberChanged = BehaviorSubject.create<GroupMember>()
    private val contactInfo = ContactInfo()

    fun setGroupById(groupId: String?) {
        if (groupId == null) {
            group = null
            return
        }

        disposableGroup.add(on<DataHandler>().getGroup(groupId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            group = it

            on<RefreshHandler>().refreshGroup(groupId)
        }, { on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.group_not_found), on<ResourcesHandler>().resources.getString(R.string.group_not_found_description)) {
            groupNotFound.onNext(Unit)
        } }))
    }

    private fun onGroupSet(group: Group) {
        if (group.direct) {
            contactInfoChanged.onNext(contactInfo.also {
                it.contactNames.clear()
                it.contactInvites.clear()
            })
        }

        disposableGroup.add(on<StoreHandler>().store.box(GroupContact::class)
                .query()
                .equal(GroupContact_.groupId, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupContacts ->
                    groupContact = groupContacts.firstOrNull { it.contactId == on<PersistenceHandler>().phoneId!! }

                    on<GroupContactsHandler>().setCurrentGroupContacts(groupContacts)

                    if (!group.direct) {
                        contactInfo.contactNames.clear()
                        for (groupContact in groupContacts) {
                            contactInfo.contactNames.add(on<NameHandler>().getName(groupContact))
                        }

                        contactInfoChanged.onNext(contactInfo)
                    }
                })

        disposableGroup.add(on<StoreHandler>().store.box(GroupInvite::class)
                .query()
                .equal(GroupInvite_.group, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupInvites ->
                    if (!group.direct) {
                        contactInfo.contactInvites.clear()
                        for (groupInvite in groupInvites) {
                            contactInfo.contactInvites.add(on<ResourcesHandler>().resources.getString(R.string.contact_invited_inline, on<NameHandler>().getName(groupInvite)))
                        }
                        contactInfoChanged.onNext(contactInfo)
                    }
                })
    }

    private fun setEventById(eventId: String?) {
        if (eventId == null) {
            return
        }

        disposableGroup.add(on<DataHandler>().getEvent(eventId)
                .subscribe({ event -> eventChanged.onNext(event) },
                        { on<DefaultAlerts>().thatDidntWork() }))
    }

    private fun setPhoneById(phoneId: String?) {
        if (phoneId == null) {
            return
        }

        on<RefreshHandler>().refreshPhone(phoneId)

        disposableGroup.add(on<StoreHandler>().store.box(Phone::class).query()
                .equal(Phone_.id, phoneId)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer {
                    if (it.isEmpty()) return@observer

                    val phone = it.first()

                    if (phoneChanged.value?.id != phone?.id) {
                        phoneChanged.onNext(phone)
                    }

                    phoneUpdated.onNext(phone)
                })
    }

    fun onGroupNotFound(disposableGroup: DisposableGroup? = null, callback: (Unit) -> Unit) = onChangeCallback(groupNotFound, callback, disposableGroup)
    fun onGroupChanged(disposableGroup: DisposableGroup? = null, callback: (Group) -> Unit) = onChangeCallback(groupChanged, callback, disposableGroup)
    fun onGroupUpdated(disposableGroup: DisposableGroup? = null, callback: (Group) -> Unit) = onChangeCallback(groupUpdated, callback, disposableGroup)
    fun onEventChanged(disposableGroup: DisposableGroup? = null, callback: (Event) -> Unit) = onChangeCallback(eventChanged, callback, disposableGroup)
    fun onPhoneChanged(disposableGroup: DisposableGroup? = null, callback: (Phone) -> Unit) = onChangeCallback(phoneChanged, callback, disposableGroup)
    fun onPhoneUpdated(disposableGroup: DisposableGroup? = null, callback: (Phone) -> Unit) = onChangeCallback(phoneUpdated, callback, disposableGroup)
    fun onContactInfoChanged(disposableGroup: DisposableGroup? = null, callback: (ContactInfo) -> Unit) = onChangeCallback(contactInfoChanged, callback, disposableGroup)
    fun onGroupMemberChanged(disposableGroup: DisposableGroup? = null, callback: (GroupMember) -> Unit) = onChangeCallback(groupMemberChanged, callback, disposableGroup)

    private fun <T> onChangeCallback(observable: Observable<T>, callback: (T) -> Unit, disposableGroup: DisposableGroup? = null) {
        (disposableGroup ?: on<DisposableHandler>().self()).add(observable.observeOn(AndroidSchedulers.mainThread()).subscribe({ callback.invoke(it) }, connectionError))
    }
}

class ContactInfo {
    val contactNames = mutableListOf<String>()
    val contactInvites = mutableListOf<String>()
}