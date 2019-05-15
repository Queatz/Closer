package closer.vlllage.com.closer.handler.group

import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupDraft
import closer.vlllage.com.closer.store.models.GroupDraft_

class GroupDraftHandler constructor(private val on: On) {
    fun saveDraft(group: Group, message: String) {
        on<StoreHandler>().store.box(GroupDraft::class.java)
                .query()
                .equal(GroupDraft_.groupId, group.id!!)
                .build().subscribe().single()
                .observer { groupDrafts ->
                    val groupDraft: GroupDraft

                    if (groupDrafts.isEmpty()) {
                        groupDraft = GroupDraft().apply {
                            groupId = group.id
                            this.message = message
                        }
                    } else {
                        groupDraft = groupDrafts[0]
                    }

                    groupDraft.message = message
                    on<StoreHandler>().store.box(GroupDraft::class.java).put(groupDraft)
                }
    }

    fun getDraft(group: Group): String? {
        val draft = on<StoreHandler>().store.box(GroupDraft::class.java)
                .query()
                .equal(GroupDraft_.groupId, group.id!!)
                .build()
                .findFirst()

        return draft?.message ?: ""

    }
}
