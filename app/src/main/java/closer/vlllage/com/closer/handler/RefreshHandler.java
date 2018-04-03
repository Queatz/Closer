package closer.vlllage.com.closer.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;

public class RefreshHandler extends PoolMember {

    public void refreshAll() {
        refreshMyGroups();
        refreshMyMessages();
    }

    public void refreshMyMessages() {
        $(DisposableHandler.class).add($(ApiHandler.class).myMessages().subscribe(messages -> {
            // todo Insert all new messages
        }, error -> $(DefaultAlerts.class).syncError()));
    }

    public void refreshMyGroups() {
        $(DisposableHandler.class).add($(ApiHandler.class).myGroups().subscribe(groups -> {
            Set<String> allMyGroupIds = new HashSet<>();
            for (GroupResult groupResult : groups) {
                allMyGroupIds.add(groupResult.id);
            }

            $(StoreHandler.class).findAll(Group.class, Group_.id, allMyGroupIds).observer(existingGroups -> {
                Collection<String> existingGroupIds = new HashSet<>();
                for (Group existingGroup : existingGroups) {
                    existingGroupIds.add(existingGroup.getId());
                }

                List<Group> groupsToAdd = new ArrayList<>();

                for (GroupResult groupResult : groups) {
                    if (!existingGroupIds.contains(groupResult.id)) {
                        groupsToAdd.add(createGroupFromGroupResult(groupResult));
                    }
                }

                $(StoreHandler.class).getStore().box(Group.class).put(groupsToAdd);
                $(StoreHandler.class).removeAllExcept(Group.class, Group_.id, allMyGroupIds);
            });
        }, error -> $(DefaultAlerts.class).syncError()));
    }

    private Group createGroupFromGroupResult(GroupResult groupResult) {
        Group group = new Group();
        group.setId(groupResult.id);
        group.setName(groupResult.name);
        return group;
    }
}
