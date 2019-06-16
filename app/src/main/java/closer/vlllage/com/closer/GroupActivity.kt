package closer.vlllage.com.closer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.FeatureHandler
import closer.vlllage.com.closer.handler.FeatureType
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.ui.CircularRevealActivity
import org.greenrobot.essentials.StringUtils

class GroupActivity : CircularRevealActivity() {

    lateinit var view: GroupViewHolder
    lateinit var groupId: String

    private var contentView: ContentViewType = ContentViewType.MESSAGES
        set(value) {
            field = value
            setContent(when (field) {
                ContentViewType.MESSAGES -> GroupMessagesFragment()
                ContentViewType.SHARE -> ShareGroupFragment()
                ContentViewType.CONTACTS -> GroupContactsFragment()
                ContentViewType.PHONE_MESSAGES -> PhoneMessagesFragment()
                ContentViewType.PHONE_PHOTOS -> PhonePhotosFragment()
                ContentViewType.PHONE_GROUPS -> PhoneGroupsFragment()

            })
        }

    enum class ContentViewType {
        MESSAGES,
        SHARE,
        CONTACTS,
        PHONE_MESSAGES,
        PHONE_PHOTOS,
        PHONE_GROUPS,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)
        view = GroupViewHolder(findViewById(android.R.id.content))

        bindViewEvents()
        bindToGroup()

        if (on<FeatureHandler>().has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
            view.settingsButton.visible = true
        }

        on<TimerHandler>().postDisposable(Runnable {
            on<RefreshHandler>().refreshAll()
        }, 1625)

        handleIntent(intent)

        on<GroupToolbarHandler>().attach(view.eventToolbar)

        on<DisposableHandler>().add(on<GroupToolbarHandler>().contentView
                .subscribe { contentView = it })

        on<MiniWindowHandler>().attach(view.groupName, view.backgroundColor) { finish() }

        on<DisposableHandler>().add(on<LightDarkHandler>().onLightChanged.subscribe {
            view.closeButton.imageTintList = it.tint
            view.scopeIndicatorButton.imageTintList = it.tint
            view.settingsButton.imageTintList = it.tint
            view.notificationSettingsButton.imageTintList = it.tint
            view.groupName.setTextColor(it.text)
            view.groupAbout.setTextColor(it.text)
            view.groupDetails.setTextColor(it.text)
        })
    }

    private fun bindToGroup() {
        on<GroupHandler> {
            onGroupMemberChanged { groupMember ->
                view.notificationSettingsButton.visible = groupMember.muted
            }

            onGroupUpdated { group ->
                setGroupBackground(group)
            }

            onContactInfoChanged { redrawContacts(it) }

            onGroupChanged { group ->
                showGroupName(group)
                view.peopleInGroup.text = ""

                if (on<Val>().isEmpty(group.about)) {
                    view.groupAbout.visible = false
                } else {
                    view.groupAbout.visible = true
                    view.groupAbout.text = group.about
                }

                on<GroupToolbarHandler>().contentView.onNext(if (group.hasPhone())
                    ContentViewType.PHONE_MESSAGES else ContentViewType.MESSAGES)

                on<GroupScopeHandler>().setup(group, view.scopeIndicatorButton)

                view.peopleInGroup.isSelected = true

                view.profilePhoto.visible = group.hasPhone()

                setGroupBackground(group)
            }

            onEventChanged { event ->
                view.groupDetails.visible = true
                view.groupDetails.text = on<EventDetailsHandler>().formatEventDetails(event)
            }

            onPhoneChanged { phone ->
                view.groupDetails.visible = false
                if (phone.photo != null) {
                    view.profilePhoto.visible = true
                    on<ImageHandler>().get().load(phone.photo + "?s=512")
                            .into(view.profilePhoto)
                    view.profilePhoto.setOnClickListener { on<PhotoActivityTransitionHandler>().show(view.profilePhoto, phone.photo!!) }

                } else {
                    view.profilePhoto.visible = false
                }
            }

            onGroupUpdated { group ->
                setGroupBackground(group)
            }

            onContactInfoChanged { redrawContacts(it) }

            onGroupChanged { group ->
                showGroupName(group)
                view.groupDetails.visible = false
                view.groupDetails.text = ""
                view.peopleInGroup.text = ""

                if (on<Val>().isEmpty(group.about)) {
                    view.groupAbout.visible = false
                } else {
                    view.groupAbout.visible = true
                    view.groupAbout.text = group.about
                }

                on<GroupScopeHandler>().setup(group, view.scopeIndicatorButton)

                view.peopleInGroup.isSelected = true

                view.profilePhoto.visible = group.hasPhone()

                setGroupBackground(group)

                on<LightDarkHandler>().setLight(group.hasPhone())

                on<ApplicationHandler>().app.on<TopHandler>().setGroupActive(group.id!!)
            }

            onEventChanged { event ->
                view.groupDetails.visible = true
                view.groupDetails.text = on<EventDetailsHandler>().formatEventDetails(event)
            }

            onPhoneChanged { phone ->
                view.groupDetails.visible = false
                view.groupDetails.text = ""
                view.groupAbout.visible = !phone.status.isNullOrBlank()
                view.groupAbout.text = phone.status ?: ""
                if (phone.photo != null) {
                    view.profilePhoto.visible = true
                    on<ImageHandler>().get().load(phone.photo + "?s=512")
                            .into(view.profilePhoto)
                    view.profilePhoto.setOnClickListener { on<PhotoActivityTransitionHandler>().show(view.profilePhoto, phone.photo!!) }

                } else {
                    view.profilePhoto.visible = false
                }
            }
        }
    }

    private fun redrawContacts(contactInfo: ContactInfo) {
        val names = mutableListOf<String>()
        names.addAll(contactInfo.contactNames)
        names.addAll(contactInfo.contactInvites)

        if (names.isEmpty()) {
            view.peopleInGroup.visible = false
            view.peopleInGroup.setText(R.string.add_contact)
            return
        }

        view.peopleInGroup.visible = true

        view.peopleInGroup.text = StringUtils.join(names, ", ")
    }

    private fun showGroupName(group: Group?) {
        if (group == null) {
            view.groupName.setText(R.string.not_found)
            return
        }

        if (group.hasPhone()) {
            on<DisposableHandler>().add(on<DataHandler>().getPhone(group.phoneId!!).subscribe(
                    { phone -> view.groupName.text = phone.name }, { on<DefaultAlerts>().thatDidntWork() }
            ))
        } else {
            view.groupName.text = on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.app_name))
        }
    }

    private fun setGroupBackground(group: Group) {
        view.backgroundColor.setBackgroundResource(on<GroupColorHandler>().getColorBackground(group))
        view.backgroundPhoto.visible = group.photo != null

        if (group.photo != null) {
            view.backgroundPhoto.setImageDrawable(null)
            on<PhotoLoader>().softLoad(group.photo!!, view.backgroundPhoto)
        }
    }

    private fun bindViewEvents() {
        view.closeButton.setOnClickListener { finish() }

        view.groupAbout.setOnClickListener {
            on<DefaultAlerts>().message(
                    on<ResourcesHandler>().resources.getString(on<GroupHandler>().phone?.let { R.string.public_status } ?: R.string.about_this_group),
                    on<GroupHandler>().phone?.status ?: on<GroupHandler>().group?.about ?: ""
            )
        }

        view.peopleInGroup.setOnClickListener { toggleContactsView() }

        view.settingsButton.setOnClickListener {
            on<GroupMemberHandler>().changeGroupSettings(on<GroupHandler>().group)
        }

        view.notificationSettingsButton.setOnClickListener {
            on<GroupMemberHandler>().changeGroupSettings(on<GroupHandler>().group)
        }

        view.closeButton.visible = !on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_HIDE_CLOSE_BUTTON]
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
        setSourceBounds(intent.sourceBounds)
        reveal()
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return

        if (intent.hasExtra(EXTRA_GROUP_ID)) {
            groupId = intent.getStringExtra(EXTRA_GROUP_ID)
            on<GroupHandler>().setGroupById(groupId)
        } else if (intent.hasExtra(EXTRA_PHONE_ID)) {
            on<DisposableHandler>().add(on<DataHandler>().getGroupForPhone(intent.getStringExtra(EXTRA_PHONE_ID)).subscribe({
                groupId = it.id!!
                on<GroupHandler>().setGroupById(groupId)
            }, {
                on<DefaultAlerts>().thatDidntWork()
            }))
        }

        if (intent.hasExtra(EXTRA_RESPOND)) {
            on<GroupMessagesHandler>().setIsRespond()
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::groupId.isInitialized) {
            on<ApplicationHandler>().app.on<TopHandler>().setGroupActive(groupId)
        }
    }

    override fun onPause() {
        super.onPause()
        on<ApplicationHandler>().app.on<TopHandler>().setGroupActive(null)
    }

    override val backgroundId = R.id.background

    private fun toggleContactsView() {
        contentView = if (contentView == ContentViewType.CONTACTS) ContentViewType.MESSAGES else ContentViewType.CONTACTS
    }

    private fun setContent(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commitAllowingStateLoss()

    }

    companion object {
        const val EXTRA_GROUP_ID = "groupId"
        const val EXTRA_PHONE_ID = "phoneId"
        const val EXTRA_RESPOND = "respond"
    }
}
