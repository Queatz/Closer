package closer.vlllage.com.closer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewTreeObserver
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.group.SearchGroupHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.share.SearchGroupsHeaderAdapter
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import io.objectbox.android.AndroidScheduler

class ShareActivity : ListActivity() {

    private var searchGroupsAdapter: SearchGroupsHeaderAdapter? = null

    private var groupMessageId: String? = null
    private var phoneId: String? = null
    private var groupId: String? = null
    private var groupToShare: Group? = null
    private var data: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        `$`(ApiHandler::class.java).setAuthorization(`$`(AccountHandler::class.java).phone)

        `$`(SearchGroupHandler::class.java).hideCreateGroupOption()

        searchGroupsAdapter = SearchGroupsHeaderAdapter(`$`(PoolMember::class.java), { group, view -> onGroupSelected(group) }, null, object : SearchGroupsHeaderAdapter.OnQueryChangedListener {
            override fun onQueryChanged(query: String) {
                `$`(SearchGroupHandler::class.java).showGroupsForQuery(searchGroupsAdapter!!, query)
            }
        })

        searchGroupsAdapter!!.setActionText(`$`(ResourcesHandler::class.java).resources.getString(R.string.share))
        searchGroupsAdapter!!.setLayoutResId(R.layout.search_groups_item_light)
        searchGroupsAdapter!!.setBackgroundResId(R.drawable.clickable_green_flat)

        `$`(SearchGroupHandler::class.java).showGroupsForQuery(searchGroupsAdapter!!, "")

        val queryBuilder = `$`(StoreHandler::class.java).store.box(Group::class.java).query()
        `$`(DisposableHandler::class.java).add(queryBuilder
                .sort(`$`(SortHandler::class.java).sortGroups())
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread())
                .observer { `$`(SearchGroupHandler::class.java).setGroups(it) })

        if (intent != null) {
            groupMessageId = intent.getStringExtra(EXTRA_GROUP_MESSAGE_ID)
            phoneId = intent.getStringExtra(EXTRA_INVITE_TO_GROUP_PHONE_ID)
            groupId = intent.getStringExtra(EXTRA_SHARE_GROUP_TO_GROUP_ID)

            searchGroupsAdapter!!.setHeaderText(`$`(ResourcesHandler::class.java).resources.getString(R.string.share_to))

            if (Intent.ACTION_SEND == intent.action) {
                data = intent.data

                if (data == null) {
                    data = intent.extras!!.get(Intent.EXTRA_STREAM) as Uri
                }
            } else if (Intent.ACTION_VIEW == intent.action) {
                if (phoneId != null) {
                    searchGroupsAdapter!!.setHeaderText(`$`(ResourcesHandler::class.java).resources.getString(R.string.add_person_to, `$`(NameHandler::class.java).getName(phoneId!!)))
                    searchGroupsAdapter!!.setActionText(`$`(ResourcesHandler::class.java).resources.getString(R.string.add))
                } else if (groupId != null) {

                    groupToShare = `$`(StoreHandler::class.java).store.box(Group::class.java).query()
                            .equal(Group_.id, groupId!!)
                            .build().findFirst()

                    searchGroupsAdapter!!.setHeaderText(`$`(ResourcesHandler::class.java).resources.getString(R.string.share_group_to, `$`(Val::class.java).of(
                            groupToShare?.name, `$`(ResourcesHandler::class.java).resources.getString(R.string.group)
                    )))

                    searchGroupsAdapter!!.setActionText(`$`(ResourcesHandler::class.java).resources.getString(R.string.share))
                }
            }
        }

        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                recyclerView.adapter = searchGroupsAdapter
            }
        })
    }

    private fun onGroupSelected(group: Group) {
        if (isDone) {
            return
        }

        if (phoneId != null) {
            `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).inviteToGroup(group.id!!, phoneId!!).subscribe(
                    { successResult ->
                        if (successResult.success) {
                            `$`(ToastHandler::class.java).show(`$`(ResourcesHandler::class.java).resources.getString(R.string.added_phone, `$`(NameHandler::class.java).getName(phoneId!!)))
                            finish()
                        } else {
                            `$`(DefaultAlerts::class.java).thatDidntWork()
                        }
                    }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
        } else if (groupToShare != null) {
            `$`(GroupMessageAttachmentHandler::class.java).shareGroup(groupToShare!!, group)
            finish (Runnable { `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(null, group.id!!) })
        } else if (groupMessageId != null) {
            `$`(GroupMessageAttachmentHandler::class.java).shareGroupMessage(group.id!!, groupMessageId)
            finish (Runnable { `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(null, group.id!!) })
        } else if (data != null) {
            `$`(ToastHandler::class.java).show(R.string.sending_photo)
            `$`(PhotoUploadGroupMessageHandler::class.java).upload(data!!) { photoId ->
                val success = `$`(GroupMessageAttachmentHandler::class.java).sharePhoto(`$`(PhotoUploadGroupMessageHandler::class.java).getPhotoPathFromId(photoId), group.id!!)
                if (!success) {
                    `$`(DefaultAlerts::class.java).thatDidntWork()
                }

                finish (Runnable { `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(null, group.id!!) })
            }
        }
    }

    companion object {
        const val EXTRA_GROUP_MESSAGE_ID = "groupMessageId"
        const val EXTRA_INVITE_TO_GROUP_PHONE_ID = "inviteToGroupPhoneId"
        const val EXTRA_SHARE_GROUP_TO_GROUP_ID = "shareGroupToGroupId"
    }
}
