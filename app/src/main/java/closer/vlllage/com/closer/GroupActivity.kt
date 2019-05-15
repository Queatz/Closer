package closer.vlllage.com.closer

import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMember
import closer.vlllage.com.closer.store.models.GroupMember_
import closer.vlllage.com.closer.store.models.Group_
import closer.vlllage.com.closer.ui.CircularRevealActivity
import io.objectbox.android.AndroidScheduler

class GroupActivity : CircularRevealActivity() {

    lateinit var view: GroupViewHolder
    lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)
        view = GroupViewHolder(findViewById(android.R.id.content))

        findViewById<View>(R.id.closeButton).setOnClickListener { view -> finish() }

        on<TimerHandler>().postDisposable(Runnable { on<RefreshHandler>().refreshAll() }, 1625)

        on<GroupToolbarHandler>().attach(findViewById<RecyclerView>(R.id.eventToolbar))

        on<GroupHandler>().attach(view.groupName, view.backgroundPhoto, view.groupAbout, view.peopleInGroup, findViewById(R.id.settingsButton))
        handleIntent(intent)

        view.groupAbout.setOnClickListener { v ->
            if (on<GroupHandler>().group == null || on<GroupHandler>().group!!.about == null) {
                return@setOnClickListener
            }

            on<DefaultAlerts>().message(
                    on<ResourcesHandler>().resources.getString(R.string.about_this_group),
                    on<GroupHandler>().group!!.about!!
            )
        }

        on<GroupActionHandler>().attach(view.actionFrameLayout, findViewById<RecyclerView>(R.id.actionRecyclerView))
        on<GroupMessagesHandler>().attach(view.messagesRecyclerView, view.replyMessage, view.sendButton, view.sendMoreButton, findViewById(R.id.sendMoreLayout))
        on<PinnedMessagesHandler>().attach(view.pinnedMessagesRecyclerView)
        on<GroupMessageMentionHandler>().attach(view.mentionSuggestionsLayout, findViewById<RecyclerView>(R.id.mentionSuggestionRecyclerView)) { mention -> on<GroupMessagesHandler>().insertMention(mention) }
        on<MiniWindowHandler>().attach(view.groupName, findViewById(R.id.backgroundColor)) { this.finish() }

        findViewById<View>(R.id.settingsButton).setOnClickListener { view -> on<GroupMemberHandler>().changeGroupSettings(on<GroupHandler>().group) }

        if (on<PersistenceHandler>().phoneId != null) {
            on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupMember::class.java).query()
                    .equal(GroupMember_.group, groupId)
                    .equal(GroupMember_.phone, on<PersistenceHandler>().phoneId)
                    .build().subscribe().on(AndroidScheduler.mainThread()).observer { groupMembers ->
                        var groupMember: GroupMember? = if (groupMembers.isEmpty()) null else groupMembers[0]

                        if (groupMember == null) {
                            groupMember = GroupMember()
                        }

                        if (groupMember.muted) {
                            findViewById<View>(R.id.notificationSettingsButton).setOnClickListener { view -> on<GroupMemberHandler>().changeGroupSettings(on<GroupHandler>().group) }
                            findViewById<View>(R.id.notificationSettingsButton).visibility = View.VISIBLE
                        } else {
                            findViewById<View>(R.id.notificationSettingsButton).visibility = View.GONE
                        }
                    })
        }

        view.replyMessage.setOnClickListener { view -> on<GroupActionHandler>().show(false) }

        on<DisposableHandler>().add(on<GroupHandler>().onGroupUpdated().subscribe { group ->
            on<PinnedMessagesHandler>().show(group)
            on<GroupHandler>().setGroupBackground(group)
        })

        val connectionError = { _: Throwable -> on<ConnectionErrorHandler>().notifyConnectionError() }

        on<DisposableHandler>().add(on<GroupHandler>().onGroupChanged().subscribe({ group ->
            on<PinnedMessagesHandler>().show(group)

            findViewById<View>(R.id.backgroundColor).setBackgroundResource(on<GroupColorHandler>().getColorBackground(group))

            on<GroupScopeHandler>().setup(group, view.scopeIndicatorButton)

            view.peopleInGroup.isSelected = true
            view.peopleInGroup.setOnClickListener { view -> toggleContactsView() }

            if (!group.hasPhone()) {
                view.profilePhoto.visibility = View.GONE
            }

            on<GroupContactsHandler>().attach(group, view.contactsRecyclerView, view.searchContacts, view.showPhoneContactsButton)

            view.showPhoneContactsButton.setOnClickListener { v ->
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
                        view.showPhoneContactsButton.visibility = View.GONE
                    }
                }
            }

            on<GroupHandler>().setGroupBackground(group)
        }, connectionError))

        on<DisposableHandler>().add(on<GroupHandler>().onEventChanged().subscribe({ event ->
            view.groupDetails.visibility = View.VISIBLE
            view.groupDetails.text = on<EventDetailsHandler>().formatEventDetails(event)
        }, connectionError))

        on<DisposableHandler>().add(on<GroupHandler>().onPhoneChanged().subscribe({ phone ->
            view.groupDetails.visibility = View.GONE
            if (phone.photo != null) {
                view.profilePhoto.visibility = View.VISIBLE
                on<ImageHandler>().get().load(phone.photo + "?s=512")
                        .into(view.profilePhoto)
                view.profilePhoto.setOnClickListener { v -> on<PhotoActivityTransitionHandler>().show(view.profilePhoto, phone.photo!!) }

            } else {
                view.profilePhoto.visibility = View.GONE
            }
        }, connectionError))

        view.shareWithRecyclerView.layoutManager = LinearLayoutManager(
                view.shareWithRecyclerView.context,
                RecyclerView.VERTICAL,
                false
        )

        on<DisposableHandler>().add(on<GroupToolbarHandler>().isShareActiveObservable
                .subscribe { isShareActive ->
                    showMessagesView(!isShareActive, true)

                    val queryBuilder = on<StoreHandler>().store.box(Group::class.java).query()
                    val groups = queryBuilder.sort(on<SortHandler>().sortGroups()).notEqual(Group_.physical, true).build().find()

                    val searchGroupsAdapter = SearchGroupsAdapter(on, { group, view ->
                        val success = on<GroupMessageAttachmentHandler>().shareEvent(on<GroupHandler>().onEventChanged().getValue()!!, group)

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

                    view.shareWithRecyclerView.adapter = searchGroupsAdapter
                })
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
        showMessagesView(view.replyMessage.visibility == View.GONE, false)
        view.shareWithRecyclerView.visibility = View.GONE
    }

    private fun showMessagesView(show: Boolean, isShowingShare: Boolean) {
        on<GroupMessagesHandler>().showSendMoreOptions(false)

        if (show) {
            view.shareWithRecyclerView.visibility = View.GONE
            view.messagesLayoutGroup.visibility = View.VISIBLE
            view.membersLayoutGroup.visibility = View.GONE

            if (view.replyMessage.text.toString().isEmpty()) {
                view.sendMoreButton.visibility = View.VISIBLE
            } else {
                view.sendMoreButton.visibility = View.GONE
            }

            view.showPhoneContactsButton.visibility = View.GONE
        } else {
            view.messagesLayoutGroup.visibility = View.GONE
            view.sendMoreButton.visibility = View.GONE

            if (isShowingShare) {
                view.shareWithRecyclerView.visibility = View.VISIBLE
            } else {
                view.membersLayoutGroup.visibility = View.VISIBLE

                on<GroupActionHandler>().cancelPendingAnimation()

                if (!on<PermissionHandler>().has(READ_CONTACTS)) {
                    view.showPhoneContactsButton.visibility = View.VISIBLE
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
