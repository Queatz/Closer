package closer.vlllage.com.closer

import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.FeatureHandler
import closer.vlllage.com.closer.handler.FeatureType
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import closer.vlllage.com.closer.ui.CircularRevealActivity
import org.greenrobot.essentials.StringUtils

class GroupActivity : CircularRevealActivity() {

    lateinit var view: GroupViewHolder
    lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)
        view = GroupViewHolder(findViewById(android.R.id.content))

        bindViewEvents()
        bindToGroup()
        bindLightDark()

        if (on<FeatureHandler>().has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
            view.settingsButton.visible = true
        }

        on<TimerHandler>().postDisposable(Runnable {
            on<RefreshHandler>().refreshAll()
        }, 1625)

        handleIntent(intent)

        on<GroupToolbarHandler>().attach(view.eventToolbar)
        on<GroupActionHandler>().attach(view.actionFrameLayout, view.actionRecyclerView)
        on<GroupMessagesHandler>().attach(view.messagesRecyclerView, view.replyMessage, view.sendButton, view.sendMoreButton, view.sendMoreLayout)
        on<PinnedMessagesHandler>().attach(view.pinnedMessagesRecyclerView)
        on<GroupMessageMentionHandler>().attach(view.mentionSuggestionsLayout, view.mentionSuggestionRecyclerView) {
            mention -> on<GroupMessagesHandler>().insertMention(mention)
        }

        on<DisposableHandler>().add(on<GroupToolbarHandler>().isShareActiveObservable
                .subscribe { isShareActive ->
                    showMessagesView(!isShareActive, true)

                    val queryBuilder = on<StoreHandler>().store.box(Group::class).query()
                    val groups = queryBuilder.sort(on<SortHandler>().sortGroups()).notEqual(Group_.physical, true).build().find()

                    val searchGroupsAdapter = SearchGroupsAdapter(on, false, { group, view ->
                        val success = on<GroupMessageAttachmentHandler>().shareEvent(on<GroupHandler>().event!!, group)

                        if (success) {
                            (on<ActivityHandler>().activity as CircularRevealActivity)
                                    .finish { on<GroupActivityTransitionHandler>().showGroupMessages(view, group.id) }
                        } else {
                            on<DefaultAlerts>().thatDidntWork()
                        }
                    }, null)

                    searchGroupsAdapter.setGroups(groups)
                    searchGroupsAdapter.setActionText(on<ResourcesHandler>().resources.getString(R.string.share))
                    searchGroupsAdapter.setIsSmall(true)

                    view.shareWithRecyclerView.layoutManager = LinearLayoutManager(view.shareWithRecyclerView.context)
                    view.shareWithRecyclerView.adapter = searchGroupsAdapter
                })

        on<MiniWindowHandler>().attach(view.groupName, view.backgroundColor) { finish() }
    }

    private fun bindLightDark() {
        on<DisposableHandler>().add(on<LightDarkHandler>().onLightChanged.subscribe {
            view.closeButton.imageTintList = it.tint
            view.sendButton.imageTintList = it.tint
            view.replyMessage.setTextColor(it.text)
            view.replyMessage.setHintTextColor(it.hint)
            view.replyMessage.setBackgroundResource(it.clickableRoundedBackground)
        })
    }

    private fun bindToGroup() {
        on<GroupHandler> {
            onGroupMemberChanged { groupMember ->
                view.notificationSettingsButton.visible = groupMember.muted
            }

            onGroupUpdated { group ->
                on<PinnedMessagesHandler>().show(group)
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

                on<PinnedMessagesHandler>().show(group)

                on<GroupScopeHandler>().setup(group, view.scopeIndicatorButton)

                view.peopleInGroup.isSelected = true

                view.profilePhoto.visible = group.hasPhone()

                on<GroupContactsHandler>().attach(group, view.contactsRecyclerView, view.searchContacts, view.showPhoneContactsButton)

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
                on<PinnedMessagesHandler>().show(group)
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

                on<PinnedMessagesHandler>().show(group)

                on<GroupScopeHandler>().setup(group, view.scopeIndicatorButton)

                view.peopleInGroup.isSelected = true

                view.profilePhoto.visible = group.hasPhone()

                on<GroupContactsHandler>().attach(group, view.contactsRecyclerView, view.searchContacts, view.showPhoneContactsButton)

                setGroupBackground(group)

                on<LightDarkHandler>().setLight(group.hasPhone())
            }

            onEventChanged { event ->
                view.groupDetails.visible = true
                view.groupDetails.text = on<EventDetailsHandler>().formatEventDetails(event)
            }

            onPhoneChanged { phone ->
                view.groupDetails.visible = false
                view.groupDetails.text = ""
                view.groupAbout.visible = true
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

        view.replyMessage.setOnClickListener { on<GroupActionHandler>().show(false) }

        view.peopleInGroup.setOnClickListener { toggleContactsView() }

        view.settingsButton.setOnClickListener {
            on<GroupMemberHandler>().changeGroupSettings(on<GroupHandler>().group)
        }

        view.showPhoneContactsButton.setOnClickListener {
            if (on<PermissionHandler>().denied(READ_CONTACTS)) {
                on<AlertHandler>().make().apply {
                    title = on<ResourcesHandler>().resources.getString(R.string.enable_contacts_permission)
                    message = on<ResourcesHandler>().resources.getString(R.string.enable_contacts_permission_rationale)
                    positiveButton = on<ResourcesHandler>().resources.getString(R.string.open_settings)
                    positiveButtonCallback = { on<SystemSettingsHandler>().showSystemSettings() }
                    show()
                }
                return@setOnClickListener
            }

            on<PermissionHandler>().check(READ_CONTACTS).`when` { granted ->
                if (granted) {
                    on<GroupContactsHandler>().showContactsForQuery()
                    view.showPhoneContactsButton.visible = false
                }
            }
        }

        view.notificationSettingsButton.setOnClickListener {
            on<GroupMemberHandler>().changeGroupSettings(on<GroupHandler>().group)
        }
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
        setSourceBounds(intent.sourceBounds)
        reveal()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && intent.hasExtra(EXTRA_GROUP_ID)) {
            groupId = intent.getStringExtra(EXTRA_GROUP_ID)
            on<GroupHandler>().setGroupById(groupId)

            if (intent.hasExtra(EXTRA_RESPOND)) {
                view.replyMessage.postDelayed({
                    view.replyMessage.requestFocus()
                    on<KeyboardHandler>().showKeyboard(view.replyMessage, true)
                }, 500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        on<ApplicationHandler>().app.on<TopHandler>().setGroupActive(groupId)
    }

    override fun onPause() {
        super.onPause()
        on<ApplicationHandler>().app.on<TopHandler>().setGroupActive(null)
    }

    override val backgroundId = R.id.background

    private fun toggleContactsView() {
        on<GroupToolbarHandler>().isShareActiveObservable.onNext(false)
        showMessagesView(!view.replyMessage.visible, false)
        view.shareWithRecyclerView.visible = false
    }

    private fun showMessagesView(show: Boolean, isShowingShare: Boolean) {
        on<GroupMessagesHandler>().showSendMoreOptions(false)

        if (show) {
            view.shareWithRecyclerView.visible = false
            view.messagesLayoutGroup.visible = true
            view.membersLayoutGroup.visible = false
            view.sendMoreButton.visible = view.replyMessage.text.toString().isEmpty()
            view.showPhoneContactsButton.visible = false
        } else {
            view.messagesLayoutGroup.visible = false
            view.sendMoreButton.visible = false

            if (isShowingShare) {
                view.shareWithRecyclerView.visible = true
            } else {
                view.membersLayoutGroup.visible = true

                on<GroupActionHandler>().cancelPendingAnimation()

                if (!on<PermissionHandler>().has(READ_CONTACTS)) {
                    view.showPhoneContactsButton.visible = true
                }

                if (on<PermissionHandler>().has(READ_CONTACTS)) {
                    on<GroupContactsHandler>().showContactsForQuery()
                }

                view.searchContacts.setText("")
            }
        }
    }

    companion object {
        const val EXTRA_GROUP_ID = "groupId"
        const val EXTRA_RESPOND = "respond"
    }
}
