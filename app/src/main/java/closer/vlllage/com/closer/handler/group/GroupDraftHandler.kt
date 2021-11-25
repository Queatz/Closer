package closer.vlllage.com.closer.handler.group

import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupDraft
import closer.vlllage.com.closer.store.models.GroupDraft_
import io.objectbox.query.QueryBuilder

class GroupDraftHandler constructor(private val on: On) {
    fun saveDraft(groupId: String, message: String? = null, post: String? = null) {
        on<StoreHandler>().store.box(GroupDraft::class)
                .query()
                .equal(GroupDraft_.groupId, groupId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build().subscribe().single()
                .observer { groupDrafts ->
                    val groupDraft: GroupDraft

                    if (groupDrafts.isEmpty()) {
                        groupDraft = GroupDraft().apply { this.groupId = groupId }
                    } else {
                        groupDraft = groupDrafts[0]
                    }

                    message?.let { groupDraft.message = it }
                    post?.let { groupDraft.post = it }

                    on<StoreHandler>().store.box(GroupDraft::class).put(groupDraft)
                }
    }

    fun getDraft(groupId: String) = on<StoreHandler>().store.box(GroupDraft::class)
            .query()
            .equal(GroupDraft_.groupId, groupId, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .findFirst()
}
