package closer.vlllage.com.closer.handler.group

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.GroupResult
import closer.vlllage.com.closer.handler.FeatureHandler
import closer.vlllage.com.closer.handler.FeatureType
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.greenrobot.essentials.StringUtils
import java.util.*

class GroupHandler : PoolMember() {

    private var groupName: TextView? = null
    private var groupAbout: TextView? = null
    private var backgroundPhoto: ImageView? = null
    private var peopleInGroup: TextView? = null
    private var settingsButton: View? = null
    var group: Group? = null
        private set(group) {
            field = group

            if (groupDataSubscription != null) {
                `$`(DisposableHandler::class.java).dispose(groupDataSubscription!!)
            }

            if (group != null) {
                onGroupSet(group)
                groupChanged.onNext(group)
                setEventById(group.eventId)
                setPhoneById(group.phoneId)
                `$`(RefreshHandler::class.java).refreshGroupMessages(group.id!!)
                `$`(RefreshHandler::class.java).refreshGroupContacts(group.id!!)

                groupDataSubscription = `$`(StoreHandler::class.java).store.box(Group::class.java).query()
                        .equal(Group_.id, group.id!!)
                        .build()
                        .subscribe()
                        .onlyChanges()
                        .on(AndroidScheduler.mainThread())
                        .observer { groups ->
                            if (groups.isEmpty()) return@observer
                            redrawContacts()
                            groupUpdated.onNext(groups[0])
                            `$`(RefreshHandler::class.java).refreshGroupContacts(group.id!!)
                        }

                `$`(DisposableHandler::class.java).add(groupDataSubscription!!)
            }

            showGroupName(group)
        }
    var groupContact: GroupContact? = null
        private set
    private val groupChanged = BehaviorSubject.create<Group>()
    private val groupUpdated = PublishSubject.create<Group>()
    private val eventChanged = BehaviorSubject.create<Event>()
    private val phoneChanged = BehaviorSubject.create<Phone>()
    private var contactNames: MutableList<String> = ArrayList()
    private var contactInvites: MutableList<String> = ArrayList()
    private var groupDataSubscription: DataSubscription? = null

    fun attach(groupName: TextView, backgroundPhoto: ImageView, groupAbout: TextView, peopleInGroup: TextView, settingsButton: View) {
        this.groupName = groupName
        this.backgroundPhoto = backgroundPhoto
        this.groupAbout = groupAbout
        this.peopleInGroup = peopleInGroup
        this.settingsButton = settingsButton
    }

    fun setGroupById(groupId: String?) {
        if (groupId == null) {
            group = null
            return
        }

        group = `$`(StoreHandler::class.java).store.box(Group::class.java).query()
                .equal(Group_.id, groupId)
                .build().findFirst()

        if (this.group == null) {
            `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getGroup(groupId)
                    .map { GroupResult.from(it) }
                    .subscribe { group ->
                        `$`(RefreshHandler::class.java).refresh(group)
                        this.group = group
                    })
        }
    }

    private fun onGroupSet(group: Group) {
        setGroupContact()
        peopleInGroup!!.text = ""

        if (`$`(Val::class.java).isEmpty(group.about)) {
            groupAbout!!.visibility = View.GONE
        } else {
            groupAbout!!.visibility = View.VISIBLE
            groupAbout!!.text = group.about
        }

        `$`(DisposableHandler::class.java).add(`$`(StoreHandler::class.java).store.box(GroupContact::class.java).query()
                .equal(GroupContact_.groupId, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupContacts ->
                    `$`(GroupContactsHandler::class.java).setCurrentGroupContacts(groupContacts)
                    contactNames = ArrayList()
                    for (groupContact in groupContacts) {
                        contactNames.add(`$`(NameHandler::class.java).getName(groupContact))
                    }

                    redrawContacts()
                })

        `$`(DisposableHandler::class.java).add(`$`(StoreHandler::class.java).store.box(GroupInvite::class.java).query()
                .equal(GroupInvite_.group, group.id!!)
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer { groupInvites ->
                    contactInvites = ArrayList()
                    for (groupInvite in groupInvites) {
                        contactInvites.add(`$`(ResourcesHandler::class.java).resources.getString(R.string.contact_invited_inline, `$`(NameHandler::class.java).getName(groupInvite)))
                    }
                    redrawContacts()
                })

        if (`$`(FeatureHandler::class.java).has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
            settingsButton!!.visibility = View.VISIBLE
        }
    }

    private fun setGroupContact() {
        if (`$`(PersistenceHandler::class.java).phoneId == null) {
            return
        }

        groupContact = `$`(StoreHandler::class.java).store.box(GroupContact::class.java).query()
                .equal(GroupContact_.groupId, this.group!!.id!!)
                .equal(GroupContact_.contactId, `$`(PersistenceHandler::class.java).phoneId)
                .build().findFirst()
    }

    private fun redrawContacts() {
        val names = ArrayList<String>()
        names.addAll(contactNames)
        names.addAll(contactInvites)

        if (names.isEmpty()) {
            peopleInGroup!!.visibility = View.GONE
            peopleInGroup!!.setText(R.string.add_contact)
            return
        }

        peopleInGroup!!.visibility = View.VISIBLE

        peopleInGroup!!.text = StringUtils.join(names, ", ")
    }

    fun showGroupName(group: Group?) {
        if (group == null) {
            groupName!!.setText(R.string.not_found)
            return
        }

        if (group.hasPhone()) {
            `$`(DisposableHandler::class.java).add(`$`(DataHandler::class.java).getPhone(group.phoneId!!).subscribe(
                    { phone -> groupName!!.text = phone.name }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }
            ))
        } else {
            groupName!!.text = `$`(Val::class.java).of(group.name!!, `$`(ResourcesHandler::class.java).resources.getString(R.string.app_name))
        }
    }

    fun setGroupBackground(group: Group) {
        if (group.photo != null) {
            backgroundPhoto!!.visibility = View.VISIBLE
            backgroundPhoto!!.setImageDrawable(null)
            `$`(PhotoLoader::class.java).softLoad(group.photo!!, backgroundPhoto!!)
        } else {
            backgroundPhoto!!.visibility = View.GONE
        }
    }

    private fun setEventById(eventId: String?) {
        if (eventId == null) {
            return
        }

        `$`(DisposableHandler::class.java).add(`$`(DataHandler::class.java).getEventById(eventId)
                .subscribe({ event -> eventChanged.onNext(event) },
                        { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
    }

    private fun setPhoneById(phoneId: String?) {
        if (phoneId == null) {
            return
        }

        `$`(DisposableHandler::class.java).add(`$`(DataHandler::class.java).getPhone(phoneId)
                .subscribe({ phone -> phoneChanged.onNext(phone) },
                        { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
    }

    fun onGroupChanged(): BehaviorSubject<Group> {
        return groupChanged
    }

    fun onGroupUpdated(): PublishSubject<Group> {
        return groupUpdated
    }

    fun onEventChanged(): BehaviorSubject<Event> {
        return eventChanged
    }

    fun onPhoneChanged(): BehaviorSubject<Phone> {
        return phoneChanged
    }
}
