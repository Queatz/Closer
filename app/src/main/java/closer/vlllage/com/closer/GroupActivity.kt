package closer.vlllage.com.closer

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.FeatureHandler
import closer.vlllage.com.closer.handler.FeatureType
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_group.view.*
import java.util.*

class GroupActivity : CircularRevealActivity() {

    lateinit var view: GroupViewHolder
    lateinit var groupId: String

    private var initialContent: ContentViewType? = null

    private var restoreOption = false

    private var contentView: ContentViewType = ContentViewType.MESSAGES
        set(value) {
            field = value
            setContent(when (field) {
                ContentViewType.MESSAGES -> GroupMessagesFragment()
                ContentViewType.CONTACTS -> GroupContactsFragment()
                ContentViewType.EVENTS -> GroupEventsFragment()
                ContentViewType.REVIEWS -> GroupReviewsFragment()
                ContentViewType.GROUP_PHOTOS -> GroupPhotosFragment()
                ContentViewType.GROUP_ABOUT -> GroupAboutFragment()
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

        handleIntent(intent)

        on<GroupToolbarHandler>().attach(view.eventToolbar) {
            if ((view.profilePhoto.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentHeight == 1f) {
                return@attach
            }

            val initialHeight = view.profilePhoto.measuredHeight
            val finalHeight = 0 //on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.profilePhotoCollapsedHeight)

            ObjectAnimator.ofFloat(0f, 1f).apply {
                duration = 150
                addUpdateListener {
                    val params = view.profilePhoto.layoutParams as ConstraintLayout.LayoutParams
                    params.apply {
                        height = (initialHeight + it.animatedFraction * (finalHeight - initialHeight)).toInt()
                        matchConstraintPercentHeight = if (height == 0) 0f else 1f
                        view.profilePhoto.layoutParams = this
                    }
                }
                start()
            }
        }

        on<DisposableHandler>().add(on<GroupToolbarHandler>().contentView
                .observeOn(AndroidSchedulers.mainThread())
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
            view.backgroundPhoto.alpha = if (it.light) .15f else 1f
        })
    }

    private fun bindToGroup() {
        on<GroupHandler> {
            if (on<SettingsHandler>().get(UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME)) {
                on<LightDarkHandler>().setLight(true)
            }

            onGroupNotFound {
               finish()
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
                view.settingsButton.setOnLongClickListener(null)

                on<GroupToolbarHandler>().contentView.onNext(initialContent ?: ContentViewType.MESSAGES)

                on<GroupScopeHandler>().setup(group, view.scopeIndicatorButton)

                view.peopleInGroup.isSelected = true

                if (!group.hasPhone()) {
                    setGroupProfilePhoto(null)
                }

                setGroupBackground(group)
                setGroupRating(group)

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

                if (restoreOption) {
                    view.meetLayout.visible = true
                    view.meetLayout.meetFalse.visible = false
                    view.meetLayout.meetPrompt.gravity = Gravity.START
                    view.meetLayout.meetPrompt.setPaddingRelative(
                            on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDialog),
                            view.meetLayout.meetPrompt.paddingTop,
                            view.meetLayout.meetPrompt.paddingEnd,
                            view.meetLayout.meetPrompt.paddingBottom,
                    )
                    view.meetLayout.meetTrue.setText(R.string.restore)
                    view.meetLayout.meetTrue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_history_black_24dp, 0)

                    view.meetLayout.meetPrompt.text = on<ResourcesHandler>().resources.getString(R.string.group_expired_on, on<TimeStr>().approx(group.updated?.let {
                        Calendar.getInstance(TimeZone.getDefault()).let { calendar ->
                            calendar.time = it

                            calendar.add(Calendar.MONTH, 3)

                            calendar.time
                        }
                    }, true))
                    view.meetLayout.meetTrue.setOnClickListener {
                        on<GroupMessageAttachmentHandler>().postGroupEvent(groupId, "@${on<PersistenceHandler>().phoneId} breathed new life into the group!")
                        view.meetLayout.visible = false
                    }
                }
            }

            onEventChanged { event ->
                view.groupDetails.visible = true
                view.groupDetails.text = on<EventDetailsHandler>().formatEventDetails(event)
            }

            onPhoneUpdated { phone ->
                setGroupProfilePhoto(phone.photo)
                view.groupDetails.visible = false
                view.groupDetails.text = ""
                view.groupAbout.visible = !phone.status.isNullOrBlank()
                view.groupAbout.text = phone.status ?: ""

                view.settingsButton.setOnLongClickListener {
                    on<AlertHandler>().make().apply {
                        title = "Terms of Use"
                        negativeButton = "Bad"
                        positiveButton = "Good"
                        negativeButtonCallback = {
                            updateTerms(phone, false)
                        }
                        positiveButtonCallback = {
                            updateTerms(phone, true)
                        }
                        show()
                    }

                    true
                }

                if (on<MatchHandler>().active) {
                    view.meetLayout.visible = true
                    view.meetLayout.meetFalse.visible = true
                    view.meetLayout.meetTrue.visible = true
                    view.meetLayout.meetPrompt.gravity = Gravity.CENTER
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

    private fun updateTerms(phone: Phone, good: Boolean) {
        on<ApiHandler>().terms(phone.id!!, good)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.success) {
                        on<DefaultAlerts>().message("Terms updated for ${on<NameHandler>().getName(phone)}")
                    } else {
                        on<DefaultAlerts>().thatDidntWork()
                    }
                }, {
                    on<DefaultAlerts>().thatDidntWork()
                }).also {
                    on<DisposableHandler>().add(it)
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
        view.peopleInGroup.text = names.joinToString()
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
            on<GroupNameHelper>().loadName(group, view.groupName) { it }
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

        if (group.about.isNullOrBlank()) {
            view.groupAbout.visible = false
        } else {
            view.groupAbout.visible = true
            view.groupAbout.text = group.about
        }
    }

    private fun setGroupBackground(group: Group) {
        if (group.photo != null) {
            view.backgroundPhoto.visible = true
            on<ImageHandler>().get().load(group.photo + "?s=512")
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(view.backgroundPhoto)
            view.backgroundPhoto.setOnClickListener { on<PhotoActivityTransitionHandler>().show(view.backgroundPhoto, group.photo!!) }

        } else {
            view.backgroundPhoto.visible = false
        }

        if (on<LightDarkHandler>().isLight()) {
            view.backgroundColor.setBackgroundResource(R.drawable.color_white_rounded)
        } else {
            view.backgroundColor.setBackgroundResource(on<GroupColorHandler>().getColorBackground(group))
        }
    }

    private fun setGroupProfilePhoto(photo: String?) {
        if (photo != null) {
            view.profilePhoto.visible = true
            on<ImageHandler>().get().load("$photo?s=512")
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(view.profilePhoto)
            view.profilePhoto.setOnClickListener { on<PhotoActivityTransitionHandler>().show(view.profilePhoto, photo) }

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
            on<GroupMemberHandler>().mute(on<GroupHandler>().group, false)
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

            if (intent.hasExtra(EXTRA_CONTENT)) {
                initialContent = ContentViewType.valueOf(intent.getStringExtra(EXTRA_CONTENT)!!)
            }

            if (intent.hasExtra(EXTRA_NEW_MEMBER)) {
                val disposableGroup = on<DisposableHandler>().group()
                on<GroupHandler>().onGroupChanged(disposableGroup) {
                    disposableGroup.dispose()
                    on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.youre_a_member, it.name ?: on<ResourcesHandler>().resources.getString(R.string.generic_group)))
                }
            }
        } else if (intent.hasExtra(EXTRA_PHONE_ID)) {
            val phoneId = intent.getStringExtra(EXTRA_PHONE_ID)!!
            on<RefreshHandler>().refreshGroupForPhone(phoneId)
            on<DisposableHandler>().add(on<DataHandler>().getGroupForPhone(phoneId).subscribe({
                groupId = it.id!!
                on<GroupHandler>().setGroupById(groupId)

                if (intent.hasExtra(EXTRA_RESPOND) && it.phoneId != null) {
                    reply(it.phoneId!!)
                }
            }, {
                on<DefaultAlerts>().thatDidntWork()
            }))
        }

        if (intent.hasExtra(EXTRA_MEET)) {
            on<MatchHandler>().activate()
        }

        if (intent.hasExtra(EXTRA_RESTORE)) {
            restoreOption = true
        }
    }

    private fun reply(phoneId: String) {
        on<DisposableHandler>().add(on<DataHandler>().getPhone(phoneId).subscribe(
                { on<ReplyHandler>().reply(it) }, { on<DefaultAlerts>().thatDidntWork() }
        ))
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
        const val EXTRA_CONTENT = "content"
        const val EXTRA_RESTORE = "restore"
    }
}
