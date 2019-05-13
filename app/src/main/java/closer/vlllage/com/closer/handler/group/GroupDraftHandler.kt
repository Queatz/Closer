package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupDraft
import closer.vlllage.com.closer.store.models.GroupDraft_

class GroupDraftHandler : PoolMember() {
    fun saveDraft(group: Group, message: String) {
        `$`(StoreHandler::class.java).store.box(GroupDraft::class.java)
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
                    `$`(StoreHandler::class.java).store.box(GroupDraft::class.java).put(groupDraft)
                }
    }

    fun getDraft(group: Group): String? {
        val draft = `$`(StoreHandler::class.java).store.box(GroupDraft::class.java)
                .query()
                .equal(GroupDraft_.groupId, group.id!!)
                .build()
                .findFirst()

        return if (draft != null) {
            draft.message
        } else ""

    }
}
