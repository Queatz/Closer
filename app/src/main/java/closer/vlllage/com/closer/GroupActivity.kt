package closer.vlllage.com.closer

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import closer.vlllage.com.closer.databinding.ActivityGroupBinding
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
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*

class GroupActivity : CircularRevealActivity() {

    lateinit var groupId: String

    private lateinit var binding: ActivityGroupBinding

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
        binding = ActivityGroupBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        bindViewEvents()
        bindToGroup()

        if (on<FeatureHandler>().has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
            binding.settingsButton.visible = true
        }

        handleIntent(intent)

        on<GroupToolbarHandler>().attach(binding.eventToolbar) {
            if ((binding.profilePhoto.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentHeight == 1f) {
                return@attach
            }

            val initialHeight = binding.profilePhoto.measuredHeight
            val finalHeight = 0 //on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.profilePhotoCollapsedHeight)

            ObjectAnimator.ofFloat(0f, 1f).apply {
                duration = 150
                addUpdateListener {
                    val params = binding.profilePhoto.layoutParams as ConstraintLayout.LayoutParams
                    params.apply {
                        height = (initialHeight + it.animatedFraction * (finalHeight - initialHeight)).toInt()
                        matchConstraintPercentHeight = if (height == 0) 0f else 1f
                        binding.profilePhoto.layoutParams = this
                    }
                }
                start()
            }
        }

        on<DisposableHandler>().add(on<GroupToolbarHandler>().contentView
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contentView = it })

        on<MiniWindowHandler>().attach(binding.groupName, binding.backgroundColor) { finish() }

        on<DisposableHandler>().add(on<LightDarkHandler>().onLightChanged.subscribe {
            binding.closeButton.imageTintList = it.tint
            binding.scopeIndicatorButton.imageTintList = it.tint
            binding.settingsButton.imageTintList = it.tint
            binding.notificationSettingsButton.imageTintList = it.tint
            binding.groupName.setTextColor(it.text)
            binding.groupAbout.setTextColor(it.text)
            binding.groupDetails.setTextColor(it.text)
            binding.groupRatingCount.setTextColor(it.text)
            binding.peopleInGroup.setTextColor(it.text)
            binding.backgroundPhoto.alpha = if (it.light) .15f else 1f
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
                binding.notificationSettingsButton.visible = groupMember.muted
            }

            onGroupUpdated { group ->
                setGroupBackground(group)
                setGroupRating(group)
                setGroupAbout(group)
            }

            onContactInfoChanged { redrawContacts(it) }

            onGroupChanged { group ->
                binding.settingsButton.setOnLongClickListener(null)

                on<GroupToolbarHandler>().contentView.onNext(initialContent ?: ContentViewType.MESSAGES)

                on<GroupScopeHandler>().setup(group, binding.scopeIndicatorButton)

                binding.peopleInGroup.isSelected = true

                if (!group.hasPhone()) {
                    setGroupProfilePhoto(null)
                }

                setGroupBackground(group)
                setGroupRating(group)

                showGroupName(group)

                if (!group.hasEvent()) {
                    binding.groupDetails.visible = false
                    binding.groupDetails.text = ""
                }

                binding.peopleInGroup.text = ""

                setGroupAbout(group)

                on<GroupScopeHandler>().setup(group, binding.scopeIndicatorButton)

                binding.peopleInGroup.isSelected = true

                setGroupBackground(group)

                on<ApplicationHandler>().app.on<TopHandler>().setGroupActive(group.id!!)

                if (restoreOption) {
                    binding.meetLayout.visible = true
                    binding.meetFalse.visible = false
                    binding.meetPrompt.gravity = Gravity.START
                    binding.meetPrompt.setPaddingRelative(
                            on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDialog),
                            binding.meetPrompt.paddingTop,
                            binding.meetPrompt.paddingEnd,
                            binding.meetPrompt.paddingBottom,
                    )
                    binding.meetTrue.setText(R.string.restore)
                    binding.meetTrue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_history_black_24dp, 0)

                    binding.meetPrompt.text = on<ResourcesHandler>().resources.getString(R.string.group_expired_on, on<TimeStr>().approx(group.updated?.let {
                        Calendar.getInstance(TimeZone.getDefault()).let { calendar ->
                            calendar.time = it

                            calendar.add(Calendar.MONTH, 3)

                            calendar.time
                        }
                    }, true))
                    binding.meetTrue.setOnClickListener {
                        on<GroupMessageAttachmentHandler>().postGroupEvent(groupId, "@${on<PersistenceHandler>().phoneId} breathed new life into the group!")
                        binding.meetLayout.visible = false
                    }
                }

                binding.groupDetails.setOnClickListener(null)
            }

            onEventChanged { event ->
                binding.groupDetails.visible = true
                binding.groupDetails.text = on<EventDetailsHandler>().formatEventDetails(event)
                binding.groupDetails.setOnClickListener {
                    val timeFormatter = SimpleDateFormat("MMMM d, yyyy${if (event.allDay) "" else " h:mma"}", Locale.US)
                    on<DefaultAlerts>().message("${timeFormatter.format(event.startsAt!!)} to ${timeFormatter.format(event.endsAt!!)}")
                }
            }

            onPhoneUpdated { phone ->
                setGroupProfilePhoto(phone.photo)
                binding.groupDetails.visible = false
                binding.groupDetails.text = ""
                binding.groupAbout.visible = !phone.status.isNullOrBlank()
                binding.groupAbout.text = phone.status ?: ""

                binding.settingsButton.setOnLongClickListener {
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
                    binding.meetLayout.visible = true
                    binding.meetFalse.visible = true
                    binding.meetTrue.visible = true
                    binding.meetPrompt.gravity = Gravity.CENTER
                    binding.meetPrompt.text = on<ResourcesHandler>().resources.getString(R.string.want_to_meet_phone, on<NameHandler>().getName(phone))
                    binding.meetTrue.setOnClickListener {
                        on<MeetHandler>().meet(phone.id!!, true)
                        if (!on<MeetHandler>().next()) finish()
                    }
                    binding.meetFalse.setOnClickListener {
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
            binding.peopleInGroup.visible = false
            binding.peopleInGroup.setText(R.string.add_contact)
            return
        }

        binding.peopleInGroup.visible = true
        binding.peopleInGroup.text = names.joinToString()
    }

    private fun showGroupName(group: Group?) {
        if (group == null) {
            binding.groupName.setText(R.string.not_found)
            return
        }

        if (group.hasPhone()) {
            on<DisposableHandler>().add(on<DataHandler>().getPhone(group.phoneId!!).subscribe(
                    { binding.groupName.text = on<NameHandler>().getName(it) }, { on<DefaultAlerts>().thatDidntWork() }
            ))
        } else {
            on<GroupNameHelper>().loadName(group, binding.groupName) { it }
        }
    }

    private fun setGroupRating(group: Group) {
        val visible = group.ratingAverage != null && group.ratingCount != null

        binding.groupRatingAverage.visible = visible
        binding.groupRatingCount.visible = visible

        if (!visible) {
            return
        }

        binding.groupRatingAverage.rating = group.ratingAverage!!.toFloat()
        binding.groupRatingCount.text = on<ResourcesHandler>().resources.getQuantityString(R.plurals.review_count_parenthesized, group.ratingCount!!, group.ratingCount)
    }

    private fun setGroupAbout(group: Group) {
        binding.groupAbout.visible = !group.about.isNullOrBlank()
        binding.groupAbout.text = group.about
    }

    private fun setGroupBackground(group: Group) {
        if (group.photo != null) {
            binding.backgroundPhoto.visible = true
            on<ImageHandler>().get().load(group.photo + "?s=512")
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.backgroundPhoto)
            binding.backgroundPhoto.setOnClickListener { on<PhotoActivityTransitionHandler>().show(binding.backgroundPhoto, group.photo!!) }

        } else {
            binding.backgroundPhoto.visible = false
        }

        if (on<LightDarkHandler>().isLight()) {
            binding.backgroundColor.setBackgroundResource(R.drawable.color_white_rounded)
        } else {
            binding.backgroundColor.setBackgroundResource(on<GroupColorHandler>().getColorBackground(group))
        }
    }

    private fun setGroupProfilePhoto(photo: String?) {
        if (photo != null) {
            binding.profilePhoto.visible = true
            on<ImageHandler>().get().load("$photo?s=512")
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.profilePhoto)
            binding.profilePhoto.setOnClickListener { on<PhotoActivityTransitionHandler>().show(binding.profilePhoto, photo) }

        } else {
            binding.profilePhoto.visible = false
        }
    }

    private fun bindViewEvents() {
        binding.closeButton.setOnClickListener { finish() }

        binding.groupAbout.setOnClickListener {
            on<DefaultAlerts>().message(
                on<GroupHandler>().phone?.let {
                    if (it.id == on<PersistenceHandler>().phoneId) on<ResourcesHandler>().resources.getString(R.string.public_status) else
                        on<ResourcesHandler>().resources.getString(
                            R.string.person_s_status,
                            on<NameHandler>().getName(it)
                        )

                } ?: on<ResourcesHandler>().resources.getString(R.string.about_this_group),
                on<GroupHandler>().phone?.status ?: on<GroupHandler>().group?.about ?: ""
            )
        }

        binding.peopleInGroup.setOnClickListener { toggleContactsView() }

        binding.settingsButton.setOnClickListener {
            on<GroupMemberHandler>().changeGroupSettings(on<GroupHandler>().group)
        }

        binding.notificationSettingsButton.setOnClickListener {
            on<GroupMemberHandler>().mute(on<GroupHandler>().group, false)
        }

        binding.closeButton.visible = !on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_HIDE_CLOSE_BUTTON]
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
