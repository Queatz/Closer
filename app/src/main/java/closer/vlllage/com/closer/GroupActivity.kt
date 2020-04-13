package closer.vlllage.com.closer

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.FeatureHandler
import closer.vlllage.com.closer.handler.FeatureType
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MeetHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.ReplyHandler
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.ui.CircularRevealActivity
import kotlinx.android.synthetic.main.activity_group.view.*
import org.greenrobot.essentials.StringUtils

class GroupActivity : CircularRevealActivity() {

    lateinit var view: GroupViewHolder
    lateinit var groupId: String

    private var contentView: ContentViewType = ContentViewType.MESSAGES
        set(value) {
            field = value
            setContent(when (field) {
                ContentViewType.MESSAGES -> GroupMessagesFragment()
                ContentViewType.CONTACTS -> GroupContactsFragment()
                ContentViewType.EVENTS -> GroupEventsFragment()
                ContentViewType.REVIEWS -> GroupReviewsFragment()
                ContentViewType.PHONE_MESSAGES -> PhoneMessagesFragment()
                ContentViewType.PHONE_PHOTOS -> PhonePhotosFragment()
                ContentViewType.PHONE_GROUPS -> PhoneGroupsFragment()
                ContentViewType.PHONE_ABOUT -> PhoneAboutFragment()
                else -> Fragment()
            })
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

        on<GroupToolbarHandler>().attach(view.eventToolbar) {
            if ((view.profilePhoto.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentHeight == 1f) {
                return@attach
            }

            val initialHeight = view.profilePhoto.measuredHeight
            val finalHeight = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.profilePhotoCollapsedHeight)

            ObjectAnimator.ofFloat(0f, 1f).apply {

                addUpdateListener {
                    val params = view.profilePhoto.layoutParams as ConstraintLayout.LayoutParams
                    params.apply {
                        matchConstraintPercentHeight = 1f
                        height = (initialHeight + it.animatedFraction * (finalHeight - initialHeight)).toInt()
                        view.profilePhoto.layoutParams = this
                    }
                    view.profilePhoto.alpha = 1f - (0.66f * it.animatedFraction)
                }
                start()
            }
        }

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
            view.groupRatingCount.setTextColor(it.text)
            view.peopleInGroup.setTextColor(it.text)
        })
    }

    private fun bindToGroup() {
        on<GroupHandler> {
            if (on<SettingsHandler>().get(UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME)) {
                on<LightDarkHandler>().setLight(true)
            }

            onGroupMemberChanged { groupMember ->
                view.notificationSettingsButton.visible = groupMember.muted
            }

            onGroupUpdated { group ->
                setGroupBackground(group)
                setGroupRating(group)
                setGroupAbout(group)
            }

            onContactInfoChanged { redrawContacts(it) }

            onGroupChanged { group ->
                if (!on<SettingsHandler>().get(UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME)) {
                    on<LightDarkHandler>().setLight(group.hasPhone())
                }

                on<GroupToolbarHandler>().contentView.onNext(if (group.hasPhone())
                    ContentViewType.PHONE_ABOUT else ContentViewType.MESSAGES)

                on<GroupScopeHandler>().setup(group, view.scopeIndicatorButton)

                view.peopleInGroup.isSelected = true

                view.profilePhoto.visible = group.hasPhone()

                setGroupBackground(group)
                setGroupRating(group)

                if (group.photo != null) {
                    view.profilePhoto.visible = true
                    on<ImageHandler>().get().load(group.photo + "?s=512")
                            .into(view.profilePhoto)
                    view.profilePhoto.setOnClickListener {
                        on<PhotoActivityTransitionHandler>().show(view.profilePhoto, group.photo!!)
                    }

                } else {
                    view.profilePhoto.visible = false
                }

                showGroupName(group)

                if (!group.hasEvent()) {
                    view.groupDetails.visible = false
                    view.groupDetails.text = ""
                }

                view.peopleInGroup.text = ""

                setGroupAbout(group)

                on<GroupScopeHandler>().setup(group, view.scopeIndicatorButton)

                view.peopleInGroup.isSelected = true

                setGroupBackground(group)

                on<ApplicationHandler>().app.on<TopHandler>().setGroupActive(group.id!!)
            }

            onEventChanged { event ->
                view.groupDetails.visible = true
                view.groupDetails.text = on<EventDetailsHandler>().formatEventDetails(event)
            }

            onPhoneUpdated { phone ->
                setGroupBackground(phone)
                view.groupDetails.visible = false
                view.groupDetails.text = ""
                view.groupAbout.visible = !phone.status.isNullOrBlank()
                view.groupAbout.text = phone.status ?: ""

                if (on<MatchHandler>().active) {
                    view.meetLayout.visible = true
                    view.meetLayout.meetPrompt.text = on<ResourcesHandler>().resources.getString(R.string.want_to_meet_phone, on<NameHandler>().getName(phone))
                    view.meetLayout.meetTrue.setOnClickListener {
                        on<MeetHandler>().meet(phone.id!!, true)
                        if (!on<MeetHandler>().next()) finish()
                    }
                    view.meetLayout.meetFalse.setOnClickListener {
                        on<MeetHandler>().meet(phone.id!!, false)
                        if (!on<MeetHandler>().next()) finish()
                    }
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
                    { view.groupName.text = on<NameHandler>().getName(it) }, { on<DefaultAlerts>().thatDidntWork() }
            ))
        } else {
            view.groupName.text = on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.app_name))
        }
    }

    private fun setGroupRating(group: Group) {
        val visible = group.ratingAverage != null && group.ratingCount != null

        view.groupRatingAverage.visible = visible
        view.groupRatingCount.visible = visible

        if (!visible) {
            return
        }

        view.groupRatingAverage.rating = group.ratingAverage!!.toFloat()
        view.groupRatingCount.text = on<ResourcesHandler>().resources.getQuantityString(R.plurals.review_count_parenthesized, group.ratingCount!!, group.ratingCount)
    }

    private fun setGroupAbout(group: Group) {
        if (group.hasEvent()) {
            return
        }

        if (on<Val>().isEmpty(group.about)) {
            view.groupAbout.visible = false
        } else {
            view.groupAbout.visible = true
            view.groupAbout.text = group.about
        }
    }

    private fun setGroupBackground(group: Group) {
        if (group.photo != null) {
            view.profilePhoto.visible = true
            on<ImageHandler>().get().load(group.photo + "?s=512")
                    .into(view.profilePhoto)
            view.profilePhoto.setOnClickListener { on<PhotoActivityTransitionHandler>().show(view.profilePhoto, group.photo!!) }

        } else {
            view.profilePhoto.visible = false
        }

        if (on<LightDarkHandler>().isLight()) {
            view.backgroundColor.setBackgroundResource(R.drawable.color_white_rounded)
        } else {
            view.backgroundColor.setBackgroundResource(on<GroupColorHandler>().getColorBackground(group))
        }
    }

    private fun setGroupBackground(phone: Phone) {
        if (phone.photo != null) {
            view.profilePhoto.visible = true
            on<ImageHandler>().get().load(phone.photo + "?s=512")
                    .into(view.profilePhoto)
            view.profilePhoto.setOnClickListener { on<PhotoActivityTransitionHandler>().show(view.profilePhoto, phone.photo!!) }

        } else {
            view.profilePhoto.visible = false
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
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
        setSourceBounds(intent.sourceBounds)
        reveal()
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return

        if (intent.hasExtra(EXTRA_GROUP_ID)) {
            groupId = intent.getStringExtra(EXTRA_GROUP_ID)!!
            on<GroupHandler>().setGroupById(groupId)

            if (intent.hasExtra(EXTRA_RESPOND)) {
                on<GroupMessagesHandler>().setIsRespond()
            }

            if (intent.hasExtra(EXTRA_NEW_MEMBER)) {
                val disposableGroup = on<DisposableHandler>().group()
                on<GroupHandler>().onGroupChanged(disposableGroup) {
                    disposableGroup.dispose()
                    on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.youre_a_member, it.name ?: on<ResourcesHandler>().resources.getString(R.string.generic_group)))
                }
            }
        } else if (intent.hasExtra(EXTRA_PHONE_ID)) {
            on<DisposableHandler>().add(on<DataHandler>().getGroupForPhone(intent.getStringExtra(EXTRA_PHONE_ID)!!).subscribe({
                groupId = it.id!!
                on<GroupHandler>().setGroupById(groupId)

                if (intent.hasExtra(EXTRA_RESPOND) && it.phoneId != null) {
                    on<ReplyHandler>().reply(it.phoneId!!)
                }
            }, {
                on<DefaultAlerts>().thatDidntWork()
            }))
        }

        if (intent.hasExtra(EXTRA_MEET)) {
            on<MatchHandler>().activate()
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
        on<GroupToolbarHandler>().contentView.onNext(if (on<GroupToolbarHandler>().contentView.value == ContentViewType.CONTACTS) ContentViewType.MESSAGES else ContentViewType.CONTACTS)
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
        const val EXTRA_MEET = "meet"
        const val EXTRA_NEW_MEMBER = "newMember"
    }
}
