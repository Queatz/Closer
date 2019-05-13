package closer.vlllage.com.closer

import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import closer.vlllage.com.closer.handler.data.*
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

        `$`(ApiHandler::class.java).setAuthorization(`$`(AccountHandler::class.java).phone)

        `$`(TimerHandler::class.java).postDisposable(Runnable { `$`(RefreshHandler::class.java).refreshAll() }, 1625)

        `$`(GroupToolbarHandler::class.java).attach(findViewById<RecyclerView>(R.id.eventToolbar))

        `$`(GroupHandler::class.java).attach(view.groupName, view.backgroundPhoto, view.groupAbout, view.peopleInGroup, findViewById(R.id.settingsButton))
        handleIntent(intent)

        view.groupAbout.setOnClickListener { v ->
            if (`$`(GroupHandler::class.java).group == null || `$`(GroupHandler::class.java).group!!.about == null) {
                return@setOnClickListener
            }

            `$`(DefaultAlerts::class.java).message(
                    `$`(ResourcesHandler::class.java).resources.getString(R.string.about_this_group),
                    `$`(GroupHandler::class.java).group!!.about!!
            )
        }

        `$`(GroupActionHandler::class.java).attach(view.actionFrameLayout, findViewById<RecyclerView>(R.id.actionRecyclerView))
        `$`(GroupMessagesHandler::class.java).attach(view.messagesRecyclerView, view.replyMessage, view.sendButton, view.sendMoreButton, findViewById(R.id.sendMoreLayout))
        `$`(PinnedMessagesHandler::class.java).attach(view.pinnedMessagesRecyclerView)
        `$`(GroupMessageMentionHandler::class.java).attach(view.mentionSuggestionsLayout, findViewById<RecyclerView>(R.id.mentionSuggestionRecyclerView)) { mention -> `$`(GroupMessagesHandler::class.java).insertMention(mention) }
        `$`(MiniWindowHandler::class.java).attach(view.groupName, findViewById(R.id.backgroundColor)) { this.finish() }

        findViewById<View>(R.id.settingsButton).setOnClickListener { view -> `$`(GroupMemberHandler::class.java).changeGroupSettings(`$`(GroupHandler::class.java).group) }

        if (`$`(PersistenceHandler::class.java).phoneId != null) {
            `$`(DisposableHandler::class.java).add(`$`(StoreHandler::class.java).store.box(GroupMember::class.java).query()
                    .equal(GroupMember_.group, groupId)
                    .equal(GroupMember_.phone, `$`(PersistenceHandler::class.java).phoneId)
                    .build().subscribe().on(AndroidScheduler.mainThread()).observer { groupMembers ->
                        var groupMember: GroupMember? = if (groupMembers.isEmpty()) null else groupMembers[0]

                        if (groupMember == null) {
                            groupMember = GroupMember()
                        }

                        if (groupMember.muted) {
                            findViewById<View>(R.id.notificationSettingsButton).setOnClickListener { view -> `$`(GroupMemberHandler::class.java).changeGroupSettings(`$`(GroupHandler::class.java).group) }
                            findViewById<View>(R.id.notificationSettingsButton).visibility = View.VISIBLE
                        } else {
                            findViewById<View>(R.id.notificationSettingsButton).visibility = View.GONE
                        }
                    })
        }

        view.replyMessage.setOnClickListener { view -> `$`(GroupActionHandler::class.java).show(false) }

        `$`(DisposableHandler::class.java).add(`$`(GroupHandler::class.java).onGroupUpdated().subscribe { group ->
            `$`(PinnedMessagesHandler::class.java).show(group)
            `$`(GroupHandler::class.java).setGroupBackground(group)
        })

        val connectionError = { _: Throwable -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }

        `$`(DisposableHandler::class.java).add(`$`(GroupHandler::class.java).onGroupChanged().subscribe({ group ->
            `$`(PinnedMessagesHandler::class.java).show(group)

            findViewById<View>(R.id.backgroundColor).setBackgroundResource(`$`(GroupColorHandler::class.java).getColorBackground(group))

            `$`(GroupScopeHandler::class.java).setup(group, view.scopeIndicatorButton)

            view.peopleInGroup.isSelected = true
            view.peopleInGroup.setOnClickListener { view -> toggleContactsView() }

            if (!group.hasPhone()) {
                view.profilePhoto.visibility = View.GONE
            }

            `$`(GroupContactsHandler::class.java).attach(group, view.contactsRecyclerView, view.searchContacts, view.showPhoneContactsButton)

            view.showPhoneContactsButton.setOnClickListener { v ->
                if (`$`(PermissionHandler::class.java).denied(READ_CONTACTS)) {
                    `$`(AlertHandler::class.java).make().apply {
                        title = `$`(ResourcesHandler::class.java).resources.getString(R.string.enable_contacts_permission)
                        message = `$`(ResourcesHandler::class.java).resources.getString(R.string.enable_contacts_permission_rationale)
                        positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.open_settings)
                        positiveButtonCallback = { `$`(SystemSettingsHandler::class.java).showSystemSettings() }
                        show()
                    }
                    return@setOnClickListener
                }

                `$`(PermissionHandler::class.java).check(READ_CONTACTS).`when` { granted ->
                    if (granted) {
                        `$`(GroupContactsHandler::class.java).showContactsForQuery()
                        view.showPhoneContactsButton.visibility = View.GONE
                    }
                }
            }

            `$`(GroupHandler::class.java).setGroupBackground(group)
        }, connectionError))

        `$`(DisposableHandler::class.java).add(`$`(GroupHandler::class.java).onEventChanged().subscribe({ event ->
            view.groupDetails.visibility = View.VISIBLE
            view.groupDetails.text = `$`(EventDetailsHandler::class.java).formatEventDetails(event)
        }, connectionError))

        `$`(DisposableHandler::class.java).add(`$`(GroupHandler::class.java).onPhoneChanged().subscribe({ phone ->
            view.groupDetails.visibility = View.GONE
            if (phone.photo != null) {
                view.profilePhoto.visibility = View.VISIBLE
                `$`(ImageHandler::class.java).get().load(phone.photo + "?s=512")
                        .into(view.profilePhoto)
                view.profilePhoto.setOnClickListener { v -> `$`(PhotoActivityTransitionHandler::class.java).show(view.profilePhoto, phone.photo!!) }

            } else {
                view.profilePhoto.visibility = View.GONE
            }
        }, connectionError))

        view.shareWithRecyclerView.layoutManager = LinearLayoutManager(
                view.shareWithRecyclerView.context,
                RecyclerView.VERTICAL,
                false
        )

        `$`(DisposableHandler::class.java).add(`$`(GroupToolbarHandler::class.java).isShareActiveObservable
                .subscribe { isShareActive ->
                    showMessagesView(!isShareActive, true)

                    val queryBuilder = `$`(StoreHandler::class.java).store.box(Group::class.java).query()
                    val groups = queryBuilder.sort(`$`(SortHandler::class.java).sortGroups()).notEqual(Group_.physical, true).build().find()

                    val searchGroupsAdapter = SearchGroupsAdapter(`$`(GroupHandler::class.java), { group, view ->
                        val success = `$`(GroupMessageAttachmentHandler::class.java).shareEvent(`$`(GroupHandler::class.java).onEventChanged().getValue()!!, group)

                        if (success) {
                            (`$`(ActivityHandler::class.java).activity as CircularRevealActivity)
                                    .finish { `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(view, group.id) }
                        } else {
                            `$`(DefaultAlerts::class.java).thatDidntWork()
                        }
                    }, null)

                    searchGroupsAdapter.setGroups(groups)
                    searchGroupsAdapter.setActionText(`$`(ResourcesHandler::class.java).resources.getString(R.string.share))
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
            `$`(GroupHandler::class.java).setGroupById(groupId)

            if (intent.hasExtra(EXTRA_RESPOND)) {
                view.replyMessage.postDelayed({
                    view.replyMessage.requestFocus()
                    `$`(KeyboardHandler::class.java).showKeyboard(view.replyMessage, true)
                }, 500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        `$`(ApplicationHandler::class.java).app.`$`(TopHandler::class.java).setGroupActive(groupId)
    }

    override fun onPause() {
        super.onPause()
        `$`(ApplicationHandler::class.java).app.`$`(TopHandler::class.java).setGroupActive(null)
    }

    override val backgroundId = R.id.background

    private fun toggleContactsView() {
        `$`(GroupToolbarHandler::class.java).isShareActiveObservable.onNext(false)
        showMessagesView(view.replyMessage.visibility == View.GONE, false)
        view.shareWithRecyclerView.visibility = View.GONE
    }

    private fun showMessagesView(show: Boolean, isShowingShare: Boolean) {
        `$`(GroupMessagesHandler::class.java).showSendMoreOptions(false)

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

                `$`(GroupActionHandler::class.java).cancelPendingAnimation()

                if (!`$`(PermissionHandler::class.java).has(READ_CONTACTS)) {
                    view.showPhoneContactsButton.visibility = View.VISIBLE
                }

                if (`$`(PermissionHandler::class.java).has(READ_CONTACTS)) {
                    `$`(GroupContactsHandler::class.java).showContactsForQuery()
                }

                view.searchContacts.setText("")
            }
        }
    }

    companion object {

        val EXTRA_GROUP_ID = "groupId"
        val EXTRA_RESPOND = "respond"
    }
}
