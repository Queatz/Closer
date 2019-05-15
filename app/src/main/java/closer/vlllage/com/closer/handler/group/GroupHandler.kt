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
import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.greenrobot.essentials.StringUtils
import java.util.*

class GroupHandler constructor(private val on: On) {

    private lateinit var groupName: TextView
    private lateinit var groupAbout: TextView
    private lateinit var backgroundPhoto: ImageView
    private lateinit var peopleInGroup: TextView
    private lateinit var settingsButton: View
    var group: Group? = null
        private set(group) {
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
                            redrawContacts()
                            groupUpdated.onNext(groups[0])
                            on<RefreshHandler>().refreshGroupContacts(group.id!!)
                        }

                on<DisposableHandler>().add(groupDataSubscription!!)
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

        group = on<StoreHandler>().store.box(Group::class.java).query()
                .equal(Group_.id, groupId)
                .build().findFirst()

        if (this.group == null) {
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
        peopleInGroup.text = ""

        if (on<Val>().isEmpty(group.about)) {
            groupAbout.visibility = View.GONE
        } else {
            groupAbout.visibility = View.VISIBLE
            groupAbout.text = group.about
        }

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupContact::class.java).query()
                .equal(GroupContact_.groupId, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupContacts ->
                    on<GroupContactsHandler>().setCurrentGroupContacts(groupContacts)
                    contactNames = ArrayList()
                    for (groupContact in groupContacts) {
                        contactNames.add(on<NameHandler>().getName(groupContact))
                    }

                    redrawContacts()
                })

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupInvite::class.java).query()
                .equal(GroupInvite_.group, group.id!!)
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer { groupInvites ->
                    contactInvites = ArrayList()
                    for (groupInvite in groupInvites) {
                        contactInvites.add(on<ResourcesHandler>().resources.getString(R.string.contact_invited_inline, on<NameHandler>().getName(groupInvite)))
                    }
                    redrawContacts()
                })

        if (on<FeatureHandler>().has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
            settingsButton.visibility = View.VISIBLE
        }
    }

    private fun setGroupContact() {
        if (on<PersistenceHandler>().phoneId == null) {
            return
        }

        groupContact = on<StoreHandler>().store.box(GroupContact::class.java).query()
                .equal(GroupContact_.groupId, this.group!!.id!!)
                .equal(GroupContact_.contactId, on<PersistenceHandler>().phoneId)
                .build().findFirst()
    }

    private fun redrawContacts() {
        val names = ArrayList<String>()
        names.addAll(contactNames)
        names.addAll(contactInvites)

        if (names.isEmpty()) {
            peopleInGroup.visibility = View.GONE
            peopleInGroup.setText(R.string.add_contact)
            return
        }

        peopleInGroup.visibility = View.VISIBLE

        peopleInGroup.text = StringUtils.join(names, ", ")
    }

    fun showGroupName(group: Group?) {
        if (group == null) {
            groupName.setText(R.string.not_found)
            return
        }

        if (group.hasPhone()) {
            on<DisposableHandler>().add(on<DataHandler>().getPhone(group.phoneId!!).subscribe(
                    { phone -> groupName.text = phone.name }, { error -> on<DefaultAlerts>().thatDidntWork() }
            ))
        } else {
            groupName.text = on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.app_name))
        }
    }

    fun setGroupBackground(group: Group) {
        if (group.photo != null) {
            backgroundPhoto.visibility = View.VISIBLE
            backgroundPhoto.setImageDrawable(null)
            on<PhotoLoader>().softLoad(group.photo!!, backgroundPhoto)
        } else {
            backgroundPhoto.visibility = View.GONE
        }
    }

    private fun setEventById(eventId: String?) {
        if (eventId == null) {
            return
        }

        on<DisposableHandler>().add(on<DataHandler>().getEventById(eventId)
                .subscribe({ event -> eventChanged.onNext(event) },
                        { error -> on<DefaultAlerts>().thatDidntWork() }))
    }

    private fun setPhoneById(phoneId: String?) {
        if (phoneId == null) {
            return
        }

        on<DisposableHandler>().add(on<DataHandler>().getPhone(phoneId)
                .subscribe({ phone -> phoneChanged.onNext(phone) },
                        { error -> on<DefaultAlerts>().thatDidntWork() }))
    }

    fun onGroupChanged() = groupChanged
    fun onGroupUpdated() = groupUpdated
    fun onEventChanged() = eventChanged
    fun onPhoneChanged() = phoneChanged
}
