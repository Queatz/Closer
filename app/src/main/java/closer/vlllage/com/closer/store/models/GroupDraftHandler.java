package closer.vlllage.com.closer.store.models;

import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;

public class GroupDraftHandler extends PoolMember {
    public void saveDraft(Group group, String message) {
        $(StoreHandler.class).getStore().box(GroupDraft.class)
                .query()
                .equal(GroupDraft_.groupId, group.getId())
                .build().subscribe().single()
                .observer(groupDrafts -> {
                    GroupDraft groupDraft;

                    if (groupDrafts.isEmpty()) {
                        groupDraft = new GroupDraft().setGroupId(group.getId()).setMessage(message);
                    } else {
                        groupDraft = groupDrafts.get(0);
                    }

                    groupDraft.setMessage(message);
                    $(StoreHandler.class).getStore().box(GroupDraft.class).put(groupDraft);
                });
    }

    public String getDraft(Group group) {
        GroupDraft draft = $(StoreHandler.class).getStore().box(GroupDraft.class)
                .query()
                .equal(GroupDraft_.groupId, group.getId())
                .build()
                .findFirst();

        if (draft != null) {
            return draft.getMessage();
        }

        return "";
    }
}
