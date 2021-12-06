package closer.vlllage.com.closer.handler.group

import android.Manifest.permission.READ_CONTACTS
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.SetNameHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import at.bluesource.choicesdk.maps.common.LatLng
import closer.vlllage.com.closer.databinding.InviteByNumberModalBinding
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.query.QueryBuilder
import io.objectbox.reactive.DataSubscription
import java.util.*

class GroupContactsHandler constructor(private val on: On) {

    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var showPhoneContactsButton: View
    private lateinit var searchContacts: EditText
    private lateinit var phoneContactAdapter: PhoneContactAdapter
    private val currentGroupContacts = HashSet<String>()
    private var dataSubscription: DataSubscription? = null

    val isEmpty: Boolean get() = phoneContactAdapter.itemCount == 0

    fun attach(group: Group, contactsRecyclerView: RecyclerView, searchContacts: EditText, showPhoneContactsButton: View) {
        this.contactsRecyclerView = contactsRecyclerView
        this.showPhoneContactsButton = showPhoneContactsButton
        this.searchContacts = searchContacts
        phoneContactAdapter = PhoneContactAdapter(on, { phoneContact ->
            if (phoneContact.name == null) {
                on<AlertHandler>().view { InviteByNumberModalBinding.inflate(it) }.apply {
                    positiveButton = on<ResourcesHandler>().resources.getString(R.string.invite)
                    textViewId = R.id.input
                    onTextViewSubmitCallback = { name ->
                        phoneContact.name = name
                        inviteToGroup(group, phoneContact)
                    }
                    title = on<ResourcesHandler>().resources.getString(R.string.invite_to_group, group.name)
                    show()
                }
            } else {
                on<AlertHandler>().make().apply {
                    positiveButton = on<ResourcesHandler>().resources.getString(R.string.add_phone_name, phoneContact.firstName)
                    message = phoneContact.phoneNumber
                    positiveButtonCallback = { inviteToGroup(group, phoneContact) }
                    if (phoneContact.phoneId != null) {
                        negativeButton = on<ResourcesHandler>().resources.getString(R.string.profile)
                        negativeButtonCallback = { on<NavigationHandler>().showProfile(phoneContact.phoneId!!) }
                    }
                    title = on<ResourcesHandler>().resources.getString(R.string.add_phone_to_group, phoneContact.firstName, group.name)
                    show()
                }
            }
        }, { groupInvite ->
            on<AlertHandler>().make().apply {
                positiveButton = on<ResourcesHandler>().resources.getString(R.string.cancel_invite)
                message = on<ResourcesHandler>().resources.getString(R.string.confirm_cancel_invite, groupInvite.name)
                positiveButtonCallback = { cancelInvite(groupInvite) }
                show()
            }
        }, { groupContact ->
            if (on<PersistenceHandler>().phoneId == groupContact.contactId) {
                on<MenuHandler>().show(
                        MenuHandler.MenuOption(R.drawable.ic_person_black_24dp, R.string.view_profile) {
                            on<NavigationHandler>().showMyProfile()
                        },
                        MenuHandler.MenuOption(R.drawable.ic_chat_black_24dp, R.string.change_your_group_status) {
                            on<DefaultInput>().show(
                                    R.string.your_group_status,
                                    buttonRes = R.string.update,
                                    prefill = groupContact.status
                            ) {
                                on<GroupContactHandler>().updateGroupStatus(groupContact, it)
                            }
                        },
                        MenuHandler.MenuOption(R.drawable.ic_photo_black_24dp, R.string.change_your_group_photo) {
                            on<DefaultMenus>().uploadPhoto { photoId ->
                                val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)
                                on<GroupContactHandler>().updateGroupPhoto(groupContact, photo)
                            }
                        },
                        MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.leave_group_action) {
                    on<AlertHandler>().make().apply {
                        negativeButton = on<ResourcesHandler>().resources.getString(R.string.nope)
                        positiveButton = on<ResourcesHandler>().resources.getString(R.string.leave_group, group.name)
                        positiveButtonCallback = { leaveGroup(group) }
                        title = on<ResourcesHandler>().resources.getString(R.string.leave_group_title, group.name)
                        message = on<ResourcesHandler>().resources.getString(
                                if (group.isPublic) R.string.leave_public_group_message else R.string.leave_private_group_message)
                        show()
                    }
                })
            } else {
                on<NavigationHandler>().showProfile(groupContact.contactId!!)
            }
        })

        if (on<PermissionHandler>().has(READ_CONTACTS)) {
            on<DisposableHandler>().add(
                    on<PhoneContactsHandler>().allContacts.subscribe { phoneContactAdapter.setContacts(it) }
            )
        }

        contactsRecyclerView.adapter = phoneContactAdapter
        contactsRecyclerView.layoutManager = LinearLayoutManager(
                contactsRecyclerView.context,
                RecyclerView.VERTICAL,
                false
        )

        searchContacts.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                showContactsForQuery(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupInvite::class).query()
                .equal(GroupInvite_.group, group.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build().subscribe().on(AndroidScheduler.mainThread()).observer { groupInvites -> phoneContactAdapter.setInvites(groupInvites) })

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupContact::class).query()
                .equal(GroupContact_.groupId, group.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build().subscribe().on(AndroidScheduler.mainThread()).observer { groupContacts -> phoneContactAdapter.setGroupContacts(groupContacts) })
    }

    fun leaveGroup(group: Group) {
        on<DisposableHandler>().add(on<ApiHandler>().leaveGroup(group.id!!).subscribe({ successResult ->
            if (successResult.success) {
                on<DefaultAlerts>().message(
                        on<ResourcesHandler>().resources.getString(R.string.group_no_more, group.name)
                ) { on<ActivityHandler>().activity!!.finish() }
                on<RefreshHandler>().refreshMyGroups()
            } else {
                on<DefaultAlerts>().thatDidntWork()
            }
        }, { error -> on<DefaultAlerts>().thatDidntWork() }))
    }

    private fun cancelInvite(groupInvite: GroupInvite) {
        on<DisposableHandler>().add(on<ApiHandler>().cancelInvite(groupInvite.group!!, groupInvite.id!!).subscribe({ successResult ->
            if (successResult.success) {
                on<AlertHandler>().make().apply {
                    message = on<ResourcesHandler>().resources.getString(R.string.invite_cancelled)
                    positiveButton = on<ResourcesHandler>().resources.getString(R.string.ok)
                    show()
                }
                on<RefreshHandler>().refreshMyGroups()
            } else {
                on<DefaultAlerts>().thatDidntWork(successResult.error)
            }
        }) { on<DefaultAlerts>().thatDidntWork((it as? ApiError)?.error) })
    }

    private fun inviteToGroup(group: Group, phoneContact: PhoneContact) {
        val myName = on<AccountHandler>().name
        if (myName.isBlank()) {
            on<SetNameHandler>().modifyName({
                sendInviteToGroup(group, phoneContact)
            }, true)
        } else {
            sendInviteToGroup(group, phoneContact)
        }
    }

    private fun sendInviteToGroup(group: Group, phoneContact: PhoneContact) {
        val inviteToGroup = if (phoneContact.phoneId == null)
            on<ApiHandler>().inviteToGroup(group.id!!, phoneContact.name!!, phoneContact.phoneNumber!!)
        else
            on<ApiHandler>().inviteToGroup(group.id!!, phoneContact.phoneId!!)

        on<DisposableHandler>().add(inviteToGroup.subscribe({ successResult ->
            if (successResult.success) {
                val message = if (phoneContact.name == null || phoneContact.name!!.isBlank())
                    on<ResourcesHandler>().resources.getString(R.string.phone_invited, phoneContact.phoneNumber, group.name)
                else
                    on<ResourcesHandler>().resources.getString(R.string.phone_invited, phoneContact.name, group.name)
                on<ToastHandler>().show(message)
                on<RefreshHandler>().refreshMyGroups()
            } else {
                on<DefaultAlerts>().thatDidntWork(successResult.error)
            }
        }, { on<DefaultAlerts>().thatDidntWork((it as? ApiError)?.error) }))
    }

    fun showContactsForQuery(originalQuery: String) {
        val query = originalQuery.trim().toLowerCase()

        if (on<LocationHandler>().lastKnownLocation != null) {
            on<DisposableHandler>().add(on<ApiHandler>().searchPhonesNear(LatLng(
                    on<LocationHandler>().lastKnownLocation!!.latitude,
                    on<LocationHandler>().lastKnownLocation!!.longitude
            ), query).subscribe({ phoneResults ->
                for (phoneResult in phoneResults) {
                    on<RefreshHandler>().refresh(on<ApiModelHandler>().from(phoneResult))
                }
            }, { error -> on<DefaultAlerts>().thatDidntWork() }))
        }

        val phoneNumber = on<PhoneNumberHandler>().normalize(originalQuery)

        phoneContactAdapter.setPhoneNumber(phoneNumber)
        phoneContactAdapter.setIsFiltered(!originalQuery.isEmpty())

        if (!on<PermissionHandler>().has(READ_CONTACTS)) {
            showPhoneContacts(listOf(), query)
            return
        }

        on<DisposableHandler>().add(on<PhoneContactsHandler>().allContacts.subscribe({ phoneContacts -> showPhoneContacts(phoneContacts, query) }, { error -> on<DefaultAlerts>().thatDidntWork() }))
    }

    private fun showPhoneContacts(phoneContacts: List<PhoneContact>?, query: String) {
        if (dataSubscription != null) {
            on<DisposableHandler>().dispose(dataSubscription!!)
        }
        dataSubscription = on<StoreHandler>().store.box(Phone::class).query()
                .contains(Phone_.name, on<Val>().trimmed(query), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .notNull(Phone_.id)
                .greater(Phone_.updated, on<TimeAgo>().fifteenDaysAgo())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { closerContacts ->
                    val allContacts = mutableListOf<PhoneContact>()

                    if (phoneContacts != null) {
                        allContacts.addAll(phoneContacts)
                    }

                    for (phone in closerContacts) {
                        if (currentGroupContacts.contains(phone.id)) {
                            continue
                        }

                        allContacts.add(0, PhoneContact(on<NameHandler>().getName(phone), phone.status).apply {
                            phoneId = phone.id
                        })
                    }

                    if (query.isEmpty()) {
                        phoneContactAdapter.setContacts(allContacts)
                        return@observer
                    }

                    val queryPhone = query.replace("[^0-9]".toRegex(), "")

                    val contacts = mutableListOf<PhoneContact>()
                    for (contact in allContacts) {
                        if (contact.name != null) {
                            if (contact.name!!.toLowerCase().contains(query)) {
                                contacts.add(contact)
                                continue
                            }
                        }

                        if (queryPhone.isNotEmpty() && contact.phoneNumber != null) {
                            if (contact.phoneNumber!!.replace("[^0-9]".toRegex(), "").contains(queryPhone)) {
                                contacts.add(contact)
                            }
                        }
                    }

                    phoneContactAdapter.setContacts(contacts)
                }
        on<DisposableHandler>().add(dataSubscription!!)
    }

    fun showContactsForQuery() {
        showContactsForQuery(searchContacts.text.toString())
    }

    fun setCurrentGroupContacts(groupContacts: List<GroupContact>) {
        currentGroupContacts.clear()
        for (groupContact in groupContacts) {
            currentGroupContacts.add(groupContact.contactId!!)
        }
    }
}
