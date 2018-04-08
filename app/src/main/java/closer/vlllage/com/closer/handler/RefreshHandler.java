package closer.vlllage.com.closer.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import closer.vlllage.com.closer.api.models.GroupContactResult;
import closer.vlllage.com.closer.api.models.GroupInviteResult;
import closer.vlllage.com.closer.api.models.GroupMessageResult;
import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.api.models.ModelResult;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.BaseObject;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupInvite;
import closer.vlllage.com.closer.store.models.GroupInvite_;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.Box;
import io.objectbox.Property;
import io.objectbox.query.QueryBuilder;

public class RefreshHandler extends PoolMember {

    public void refreshAll() {
        refreshMyGroups();
        refreshMyMessages();
    }

    public void refreshMyMessages() {
        $(DisposableHandler.class).add($(ApiHandler.class).myMessages().subscribe(this::handleMessages, error -> $(DefaultAlerts.class).syncError()));
    }

    public void refreshMyGroups() {
        $(DisposableHandler.class).add($(ApiHandler.class).myGroups().subscribe(stateResult -> {
            handleGroups(stateResult.groups);
            handleGroupContacts(stateResult.groupContacts);
            handleFullListResult(stateResult.groupInvites, GroupInvite.class, GroupInvite_.id, this::transformGroupInviteResult);
        }, error -> $(DefaultAlerts.class).syncError()));
    }

    private void handleMessages(final List<GroupMessageResult> messages) {
        QueryBuilder<GroupMessage> query = $(StoreHandler.class).getStore().box(GroupMessage.class).query();

        boolean isFirst = true;
        for (GroupMessageResult message : messages) {
            if (isFirst) {
                isFirst = false;
            } else {
                query.or();
            }

            query.equal(GroupMessage_.id, message.id);
        }

        query.build().subscribe().single()
                .observer(groupMessages -> {
                    Set<String> existingIds = new HashSet<>();
                    for(GroupMessage groupMessage : groupMessages) {
                        existingIds.add(groupMessage.getId());
                    }

                    Box<GroupMessage> groupMessageBox = $(StoreHandler.class).getStore().box(GroupMessage.class);
                    for (GroupMessageResult message : messages) {
                        if (!existingIds.contains(message.id)) {
                            groupMessageBox.put(transformGroupMessageResult(message));
                        }
                    }
                });
    }

    private <T extends BaseObject, R extends ModelResult> void handleFullListResult(
            List<R> results, Class<T> clazz,
            Property idProperty,
            ResultTransformer<T, R> transformer) {
        Set<String> serverIdList = new HashSet<>();
        for (R obj : results) {
            serverIdList.add(obj.id);
        }

        $(StoreHandler.class).findAll(clazz, idProperty, serverIdList).observer(existingObjs -> {
            Map<String, T> existingObjsMap = new HashMap<>();
            for (T existingObj : existingObjs) {
                existingObjsMap.put(existingObj.getId(), existingObj);
            }

            List<T> objsToAdd = new ArrayList<>();

            for (R result : results) {
                if (!existingObjsMap.containsKey(result.id)) {
                    objsToAdd.add(transformer.transform(result));
                }
            }

            $(StoreHandler.class).getStore().box(clazz).put(objsToAdd);
            $(StoreHandler.class).removeAllExcept(clazz, idProperty, serverIdList);
        });
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

    private GroupInvite transformGroupInviteResult(GroupInviteResult result) {
        GroupInvite groupInvite = new GroupInvite();
        groupInvite.setId(result.id);
        groupInvite.setGroup(result.group);
        groupInvite.setName(result.name);
        return groupInvite;
    }

    private GroupMessage transformGroupMessageResult(GroupMessageResult result) {
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setId(result.id);
        groupMessage.setContactId(result.from);
        groupMessage.setGroupId(result.to);
        groupMessage.setText(result.text);
        groupMessage.setTime(result.created);
        groupMessage.setAttachment(result.attachment);
        return groupMessage;
    }

    interface ResultTransformer<T, R> {
        T transform(R result);
    }
}
