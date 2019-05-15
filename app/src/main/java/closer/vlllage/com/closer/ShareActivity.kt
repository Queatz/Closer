package closer.vlllage.com.closer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewTreeObserver
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.group.SearchGroupHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.share.SearchGroupsHeaderAdapter
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

        on<SearchGroupHandler>().hideCreateGroupOption()

        searchGroupsAdapter = SearchGroupsHeaderAdapter(on, { group, view -> onGroupSelected(group) }, null, object : SearchGroupsHeaderAdapter.OnQueryChangedListener {
            override fun onQueryChanged(query: String) {
                on<SearchGroupHandler>().showGroupsForQuery(searchGroupsAdapter!!, query)
            }
        })

        searchGroupsAdapter!!.setActionText(on<ResourcesHandler>().resources.getString(R.string.share))
        searchGroupsAdapter!!.setLayoutResId(R.layout.search_groups_item_light)
        searchGroupsAdapter!!.setBackgroundResId(R.drawable.clickable_green_flat)

        on<SearchGroupHandler>().showGroupsForQuery(searchGroupsAdapter!!, "")

        val queryBuilder = on<StoreHandler>().store.box(Group::class.java).query()
        on<DisposableHandler>().add(queryBuilder
                .sort(on<SortHandler>().sortGroups())
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread())
                .observer { on<SearchGroupHandler>().setGroups(it) })

        if (intent != null) {
            groupMessageId = intent.getStringExtra(EXTRA_GROUP_MESSAGE_ID)
            phoneId = intent.getStringExtra(EXTRA_INVITE_TO_GROUP_PHONE_ID)
            groupId = intent.getStringExtra(EXTRA_SHARE_GROUP_TO_GROUP_ID)

            searchGroupsAdapter!!.setHeaderText(on<ResourcesHandler>().resources.getString(R.string.share_to))

            if (Intent.ACTION_SEND == intent.action) {
                data = intent.data

                if (data == null) {
                    data = intent.extras!!.get(Intent.EXTRA_STREAM) as Uri
                }
            } else if (Intent.ACTION_VIEW == intent.action) {
                if (phoneId != null) {
                    searchGroupsAdapter!!.setHeaderText(on<ResourcesHandler>().resources.getString(R.string.add_person_to, on<NameHandler>().getName(phoneId!!)))
                    searchGroupsAdapter!!.setActionText(on<ResourcesHandler>().resources.getString(R.string.add))
                } else if (groupId != null) {

                    groupToShare = on<StoreHandler>().store.box(Group::class.java).query()
                            .equal(Group_.id, groupId!!)
                            .build().findFirst()

                    searchGroupsAdapter!!.setHeaderText(on<ResourcesHandler>().resources.getString(R.string.share_group_to, on<Val>().of(
                            groupToShare?.name, on<ResourcesHandler>().resources.getString(R.string.group)
                    )))

                    searchGroupsAdapter!!.setActionText(on<ResourcesHandler>().resources.getString(R.string.share))
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
            on<DisposableHandler>().add(on<ApiHandler>().inviteToGroup(group.id!!, phoneId!!).subscribe(
                    { successResult ->
                        if (successResult.success) {
                            on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.added_phone, on<NameHandler>().getName(phoneId!!)))
                            finish()
                        } else {
                            on<DefaultAlerts>().thatDidntWork()
                        }
                    }, { error -> on<DefaultAlerts>().thatDidntWork() }))
        } else if (groupToShare != null) {
            on<GroupMessageAttachmentHandler>().shareGroup(groupToShare!!, group)
            finish (Runnable { on<GroupActivityTransitionHandler>().showGroupMessages(null, group.id!!) })
        } else if (groupMessageId != null) {
            on<GroupMessageAttachmentHandler>().shareGroupMessage(group.id!!, groupMessageId)
            finish (Runnable { on<GroupActivityTransitionHandler>().showGroupMessages(null, group.id!!) })
        } else if (data != null) {
            on<ToastHandler>().show(R.string.sending_photo)
            on<PhotoUploadGroupMessageHandler>().upload(data!!) { photoId ->
                val success = on<GroupMessageAttachmentHandler>().sharePhoto(on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId), group.id!!)
                if (!success) {
                    on<DefaultAlerts>().thatDidntWork()
                }

                finish (Runnable { on<GroupActivityTransitionHandler>().showGroupMessages(null, group.id!!) })
            }
        }
    }

    companion object {
        const val EXTRA_GROUP_MESSAGE_ID = "groupMessageId"
        const val EXTRA_INVITE_TO_GROUP_PHONE_ID = "inviteToGroupPhoneId"
        const val EXTRA_SHARE_GROUP_TO_GROUP_ID = "shareGroupToGroupId"
    }
}