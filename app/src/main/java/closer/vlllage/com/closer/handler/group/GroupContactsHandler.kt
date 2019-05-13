package closer.vlllage.com.closer.handler.group

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText

import com.google.android.gms.maps.model.LatLng

import java.util.ArrayList
import java.util.HashSet

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.PhoneResult
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.LocationHandler
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.PhoneContactsHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.AlertHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.MenuHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import closer.vlllage.com.closer.handler.helpers.Val
import closer.vlllage.com.closer.handler.map.SetNameHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.PhoneMessagesHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupContact_
import closer.vlllage.com.closer.store.models.GroupInvite
import closer.vlllage.com.closer.store.models.GroupInvite_
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription

import android.Manifest.permission.READ_CONTACTS

class GroupContactsHandler : PoolMember() {

    private var contactsRecyclerView: RecyclerView? = null
    private var showPhoneContactsButton: View? = null
    private var searchContacts: EditText? = null
    private var phoneContactAdapter: PhoneContactAdapter? = null
    private val currentGroupContacts = HashSet<String>()
    private var dataSubscription: DataSubscription? = null

    val isEmpty: Boolean
        get() = phoneContactAdapter!!.itemCount == 0

    fun attach(group: Group, contactsRecyclerView: RecyclerView, searchContacts: EditText, showPhoneContactsButton: View) {
        this.contactsRecyclerView = contactsRecyclerView
        this.showPhoneContactsButton = showPhoneContactsButton
        this.searchContacts = searchContacts
        phoneContactAdapter = PhoneContactAdapter(this, object : PhoneContactAdapter.OnPhoneContactClickListener {
            override fun onPhoneContactClicked(phoneContact: PhoneContact) {
                if (phoneContact.name == null) {
                    `$`(AlertHandler::class.java).make().apply {
                        positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.invite)
                        layoutResId = R.layout.invite_by_number_modal
                        textViewId = R.id.input
                        onTextViewSubmitCallback = { name ->
                            phoneContact.name = name
                            inviteToGroup(group, phoneContact)
                        }
                        title = `$`(ResourcesHandler::class.java).resources.getString(R.string.invite_to_group, group.name)
                        show()
                    }
                } else {
                    `$`(AlertHandler::class.java).make().apply {
                        positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.add_phone_name, phoneContact.firstName)
                        message = phoneContact.phoneNumber
                        positiveButtonCallback = { alertResult -> inviteToGroup(group, phoneContact) }
                        title = `$`(ResourcesHandler::class.java).resources.getString(R.string.add_phone_to_group, phoneContact.firstName, group.name)
                        show()
                    }
                }
            }
        }, object : PhoneContactAdapter.OnGroupInviteClickListener {
            override fun onGroupInviteClicked(groupInvite: GroupInvite) {
                `$`(AlertHandler::class.java).make().apply {
                    positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.cancel_invite)
                    message = `$`(ResourcesHandler::class.java).resources.getString(R.string.confirm_cancel_invite, groupInvite.name)
                    positiveButtonCallback = { alertResult -> cancelInvite(groupInvite) }
                    show()
                }
            }
        }, object : PhoneContactAdapter.OnGroupContactClickListener {
            override fun onGroupContactClicked(groupContact: GroupContact) {
                if (`$`(PersistenceHandler::class.java).phoneId == groupContact.contactId) {
                    `$`(MenuHandler::class.java).show(MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.leave_group_action) {
                        `$`(AlertHandler::class.java).make().apply {
                            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.leave_group, group.name)
                            positiveButtonCallback = { result -> leaveGroup(group) }
                            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.leave_group_title, group.name)
                            message = `$`(ResourcesHandler::class.java).resources.getString(
                                    if (group.isPublic) R.string.leave_public_group_message else R.string.leave_private_group_message)
                            show()
                        }
                    })
                } else {
                    `$`(PhoneMessagesHandler::class.java).openMessagesWithPhone(groupContact.contactId!!, groupContact.contactName!!, "")
                }
            }
        })

        if (`$`(PermissionHandler::class.java).has(READ_CONTACTS)) {
            `$`(DisposableHandler::class.java).add(
                    `$`(PhoneContactsHandler::class.java).allContacts.subscribe({ phoneContactAdapter!!.setContacts(it) })
            )
        }

        contactsRecyclerView.adapter = phoneContactAdapter
        contactsRecyclerView.layoutManager = LinearLayoutManager(
                contactsRecyclerView.context,
                LinearLayoutManager.VERTICAL,
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

        `$`(DisposableHandler::class.java).add(`$`(StoreHandler::class.java).store.box(GroupInvite::class.java).query()
                .equal(GroupInvite_.group, group.id!!)
                .build().subscribe().on(AndroidScheduler.mainThread()).observer { groupInvites -> phoneContactAdapter!!.setInvites(groupInvites) })

        `$`(DisposableHandler::class.java).add(`$`(StoreHandler::class.java).store.box(GroupContact::class.java).query()
                .equal(GroupContact_.groupId, group.id!!)
                .build().subscribe().on(AndroidScheduler.mainThread()).observer { groupContacts -> phoneContactAdapter!!.setGroupContacts(groupContacts) })
    }

    private fun leaveGroup(group: Group) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).leaveGroup(group.id!!).subscribe({ successResult ->
            if (successResult.success) {
                `$`(DefaultAlerts::class.java).message(
                        `$`(ResourcesHandler::class.java).resources.getString(R.string.group_no_more, group.name),
                        { `$`(ActivityHandler::class.java).activity!!.finish() })
                `$`(RefreshHandler::class.java).refreshMyGroups()
            } else {
                `$`(DefaultAlerts::class.java).thatDidntWork()
            }
        }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
    }

    private fun cancelInvite(groupInvite: GroupInvite) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).cancelInvite(groupInvite.group!!, groupInvite.id!!).subscribe { successResult ->
            if (successResult.success) {
                `$`(AlertHandler::class.java).make().apply {
                    message = `$`(ResourcesHandler::class.java).resources.getString(R.string.invite_cancelled)
                    positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.ok)
                    show()
                }
                `$`(RefreshHandler::class.java).refreshMyGroups()
            } else {
                `$`(DefaultAlerts::class.java).thatDidntWork(successResult.error)
            }
        })
    }

    private fun inviteToGroup(group: Group, phoneContact: PhoneContact) {
        val myName = `$`(AccountHandler::class.java).name
        if (myName == null || myName.trim { it <= ' ' }.isEmpty()) {
            `$`(SetNameHandler::class.java).modifyName(object : SetNameHandler.OnNameModifiedCallback {
                override fun onNameModified(name: String?) {
                    sendInviteToGroup(group, phoneContact)
                }
            }, true)
        } else {
            sendInviteToGroup(group, phoneContact)
        }
    }

    private fun sendInviteToGroup(group: Group, phoneContact: PhoneContact) {
        val inviteToGroup = if (phoneContact.phoneId == null)
            `$`(ApiHandler::class.java).inviteToGroup(group.id!!, phoneContact.name!!, phoneContact.phoneNumber!!)
        else
            `$`(ApiHandler::class.java).inviteToGroup(group.id!!, phoneContact.phoneId!!)

        `$`(DisposableHandler::class.java).add(inviteToGroup.subscribe({ successResult ->
            if (successResult.success) {
                val message = if (phoneContact.name == null || phoneContact.name!!.trim { it <= ' ' }.isEmpty())
                    `$`(ResourcesHandler::class.java).resources.getString(R.string.phone_invited, phoneContact.phoneNumber, group.name)
                else
                    `$`(ResourcesHandler::class.java).resources.getString(R.string.phone_invited, phoneContact.name, group.name)
                `$`(AlertHandler::class.java).make().apply {
                    this.message = message
                    positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.yaay)
                    show()
                }
                `$`(RefreshHandler::class.java).refreshMyGroups()
            } else {
                `$`(DefaultAlerts::class.java).thatDidntWork(successResult.error)
            }
        }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
    }

    fun showContactsForQuery(originalQuery: String) {
        val query = originalQuery.trim { it <= ' ' }.toLowerCase()

        if (`$`(LocationHandler::class.java).lastKnownLocation != null) {
            `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).searchPhonesNear(LatLng(
                    `$`(LocationHandler::class.java).lastKnownLocation!!.latitude,
                    `$`(LocationHandler::class.java).lastKnownLocation!!.longitude
            ), query).subscribe({ phoneResults ->
                for (phoneResult in phoneResults) {
                    `$`(RefreshHandler::class.java).refresh(PhoneResult.from(phoneResult))
                }
            }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
        }

        val phoneNumber = `$`(PhoneNumberHandler::class.java).normalize(originalQuery)

        phoneContactAdapter!!.setPhoneNumber(phoneNumber!!)
        phoneContactAdapter!!.setIsFiltered(!originalQuery.isEmpty())

        if (!`$`(PermissionHandler::class.java).has(READ_CONTACTS)) {
            showPhoneContacts(ArrayList(), query)
            return
        }

        `$`(DisposableHandler::class.java).add(`$`(PhoneContactsHandler::class.java).allContacts.subscribe({ phoneContacts -> showPhoneContacts(phoneContacts, query) }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
    }

    private fun showPhoneContacts(phoneContacts: List<PhoneContact>?, query: String) {
        if (dataSubscription != null) {
            `$`(DisposableHandler::class.java).dispose(dataSubscription!!)
        }
        dataSubscription = `$`(StoreHandler::class.java).store.box(Phone::class.java).query()
                .contains(Phone_.name, `$`(Val::class.java).of(query))
                .notNull(Phone_.id)
                .greater(Phone_.updated, `$`(TimeAgo::class.java).fifteenDaysAgo())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { closerContacts ->
                    val allContacts = ArrayList<PhoneContact>()

                    if (phoneContacts != null) {
                        allContacts.addAll(phoneContacts)
                    }

                    for (phone in closerContacts) {
                        if (currentGroupContacts.contains(phone.id)) {
                            continue
                        }

                        allContacts.add(0, PhoneContact(`$`(NameHandler::class.java).getName(phone), phone.status).apply {
                            phoneId = phone.id
                        })
                    }

                    if (query.isEmpty()) {
                        phoneContactAdapter!!.setContacts(allContacts)
                        return@observer
                    }

                    val queryPhone = query.replace("[^0-9]".toRegex(), "")

                    val contacts = ArrayList<PhoneContact>()
                    for (contact in allContacts) {
                        if (contact.name != null) {
                            if (contact.name!!.toLowerCase().contains(query)) {
                                contacts.add(contact)
                                continue
                            }
                        }

                        if (!queryPhone.isEmpty() && contact.phoneNumber != null) {
                            if (contact.phoneNumber!!.replace("[^0-9]".toRegex(), "").contains(queryPhone)) {
                                contacts.add(contact)
                            }
                        }
                    }

                    phoneContactAdapter!!.setContacts(contacts)
                }
        `$`(DisposableHandler::class.java).add(dataSubscription!!)
    }

    fun showContactsForQuery() {
        showContactsForQuery(searchContacts!!.text.toString())
    }

    fun setCurrentGroupContacts(groupContacts: List<GroupContact>) {
        currentGroupContacts.clear()
        for (groupContact in groupContacts) {
            currentGroupContacts.add(groupContact.contactId!!)
        }
    }
}
