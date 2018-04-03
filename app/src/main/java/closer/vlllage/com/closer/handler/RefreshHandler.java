package closer.vlllage.com.closer.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import closer.vlllage.com.closer.api.models.GroupContactResult;
import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
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
        $(DisposableHandler.class).add($(ApiHandler.class).myGroups().subscribe(stateResult -> {
            handleGroups(stateResult.groups);
            handleGroupContacts(stateResult.groupContacts);
        }, error -> $(DefaultAlerts.class).syncError()));
    }

    private void handleGroupContacts(List<GroupContactResult> groupContacts) {
        Set<String> allMyGroupContactIds = new HashSet<>();
        for (GroupContactResult groupContactResult : groupContacts) {
            allMyGroupContactIds.add(groupContactResult.id);
        }

        $(StoreHandler.class).findAll(GroupContact.class, GroupContact_.id, allMyGroupContactIds).observer(existingGroupContacts -> {
            Map<String, GroupContact> existingGroupContactsMap = new HashMap<>();
            for (GroupContact existingGroupContact : existingGroupContacts) {
                existingGroupContactsMap.put(existingGroupContact.getId(), existingGroupContact);
            }

            List<GroupContact> groupsToAdd = new ArrayList<>();

            for (GroupContactResult groupContactResult : groupContacts) {
                if (!existingGroupContactsMap.containsKey(groupContactResult.id)) {
                    groupsToAdd.add(createGroupContactFromGroupContactResult(groupContactResult));
                } else {
                    existingGroupContactsMap.get(groupContactResult.id).setContactName(groupContactResult.phone.name);
                    $(StoreHandler.class).getStore().box(GroupContact.class).put(
                            existingGroupContactsMap.get(groupContactResult.id)
                    );
                }
            }

            $(StoreHandler.class).getStore().box(GroupContact.class).put(groupsToAdd);
            $(StoreHandler.class).removeAllExcept(GroupContact.class, GroupContact_.id, allMyGroupContactIds);
        });
    }

    private void handleGroups(List<GroupResult> groups) {
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
    }

    private Group createGroupFromGroupResult(GroupResult groupResult) {
        Group group = new Group();
        group.setId(groupResult.id);
        group.setName(groupResult.name);
        return group;
    }

    private GroupContact createGroupContactFromGroupContactResult(GroupContactResult groupContactResult) {
        GroupContact groupContact = new GroupContact();
        groupContact.setId(groupContactResult.id);
        groupContact.setContactId(groupContactResult.from);
        groupContact.setGroupId(groupContactResult.to);
        groupContact.setContactName(groupContactResult.phone.name);
        return groupContact;
    }
}
