package closer.vlllage.com.closer.handler;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import closer.vlllage.com.closer.api.models.EventResult;
import closer.vlllage.com.closer.api.models.GroupContactResult;
import closer.vlllage.com.closer.api.models.GroupInviteResult;
import closer.vlllage.com.closer.api.models.GroupMessageResult;
import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.api.models.ModelResult;
import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.BaseObject;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Event_;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupInvite;
import closer.vlllage.com.closer.store.models.GroupInvite_;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;
import io.objectbox.Box;
import io.objectbox.Property;
import io.objectbox.query.QueryBuilder;

import static java.lang.Boolean.TRUE;

public class RefreshHandler extends PoolMember {

    public void refreshAll() {
        refreshMyGroups();
        refreshMyMessages();
    }

    public void refreshMyMessages() {
        $(LocationHandler.class).getCurrentLocation(location -> {
            $(DisposableHandler.class).add($(ApiHandler.class).myMessages(new LatLng(
                    location.getLatitude(),
                    location.getLongitude()
            )).subscribe(this::handleMessages, error -> $(DefaultAlerts.class).syncError()));
        });
    }

    public void refreshMyGroups() {
        $(LocationHandler.class).getCurrentLocation(location -> {
            $(DisposableHandler.class).add($(ApiHandler.class).myGroups(new LatLng(
                    location.getLatitude(),
                    location.getLongitude()
            )).subscribe(stateResult -> {
                handleFullListResult(stateResult.groups, Group.class, Group_.id, true, this::createGroupFromGroupResult, this::updateGroupFromGroupResult);
                handleFullListResult(stateResult.groupInvites, GroupInvite.class, GroupInvite_.id, true, this::transformGroupInviteResult, null);
                handleGroupContacts(stateResult.groupContacts);
            }, error -> $(DefaultAlerts.class).syncError()));
        });
    }

    public void refreshEvents() {
        $(LocationHandler.class).getCurrentLocation(location -> {
            $(DisposableHandler.class).add($(ApiHandler.class).getEvents(new LatLng(
                    location.getLatitude(),
                    location.getLongitude()
            )).subscribe(eventResults -> {
                handleFullListResult(eventResults, Event.class, Event_.id, false, this::createEventFromEventResult, this::updateEventFromEventResult);
            }, error -> $(DefaultAlerts.class).syncError()));
        });
    }

    private void handleMessages(final List<GroupMessageResult> messages) {
        List<PhoneResult> phoneResults = new ArrayList<>();
        for (GroupMessageResult groupMessageResult : messages) {
            if (groupMessageResult.phone != null) {
                phoneResults.add(groupMessageResult.phone);
            }
        }

        handlePhones(phoneResults);

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
                    Map<String, GroupMessage> existingObjsMap = new HashMap<>();
                    for (GroupMessage existingObj : groupMessages) {
                        existingObjsMap.put(existingObj.getId(), existingObj);
                    }


                    Box<GroupMessage> groupMessageBox = $(StoreHandler.class).getStore().box(GroupMessage.class);
                    for (GroupMessageResult message : messages) {
                        if (!existingObjsMap.containsKey(message.id)) {
                            groupMessageBox.put(transformGroupMessageResult(message));
                        }
                    }
                });
    }

    private <T extends BaseObject, R extends ModelResult> void handleFullListResult(
            List<R> results,
            Class<T> clazz,
            Property idProperty,
            boolean deleteLocalNotReturnedFromServer,
            CreateTransformer<T, R> createTransformer,
            UpdateTransformer<T, R> updateTransformer) {
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
                    objsToAdd.add(createTransformer.transform(result));
                } else if (updateTransformer != null) {
                    objsToAdd.add(updateTransformer.transform(existingObjsMap.get(result.id), result));
                }
            }

            $(StoreHandler.class).getStore().box(clazz).put(objsToAdd);

            if (deleteLocalNotReturnedFromServer) {
                $(StoreHandler.class).removeAllExcept(clazz, idProperty, serverIdList);
            }
        });
    }

    private void handleGroupContacts(List<GroupContactResult> groupContacts) {
        Set<String> allMyGroupContactIds = new HashSet<>();
        List<PhoneResult> phoneResults = new ArrayList<>();
        for (GroupContactResult groupContactResult : groupContacts) {
            allMyGroupContactIds.add(groupContactResult.id);

            if (groupContactResult.phone != null) {
                phoneResults.add(groupContactResult.phone);
            }
        }

        handlePhones(phoneResults);

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

    private void handlePhones(List<PhoneResult> phoneResults) {
        handleFullListResult(phoneResults, Phone.class, Phone_.id, false, this::createPhoneFromResult, this::updatePhoneFromResult);
    }

    private Phone createPhoneFromResult(PhoneResult phoneResult) {
        return updatePhoneFromResult(new Phone(), phoneResult);
    }

    private Phone updatePhoneFromResult(Phone phone, PhoneResult phoneResult) {
        phone.setId(phoneResult.id);
        phone.setUpdated(phoneResult.updated);

        if (phoneResult.geo != null && phoneResult.geo.size() == 2) {
            phone.setLatitude(phoneResult.geo.get(0));
            phone.setLongitude(phoneResult.geo.get(1));
        }

        phone.setName(phoneResult.name);
        phone.setStatus(phoneResult.status);

        return phone;
    }

    private Event createEventFromEventResult(EventResult eventResult) {
        Event event = new Event();
        event.setId(eventResult.id);
        updateEventFromEventResult(event, eventResult);
        return event;
    }

    private Event updateEventFromEventResult(Event event, EventResult eventResult) {
        event.setName(eventResult.name);
        event.setAbout(eventResult.about);
        event.setLatitude(eventResult.geo.get(0));
        event.setLongitude(eventResult.geo.get(1));
        event.setEndsAt(eventResult.endsAt);
        event.setStartsAt(eventResult.startsAt);
        event.setCancelled(eventResult.cancelled);
        return event;
    }

    private Group createGroupFromGroupResult(GroupResult groupResult) {
        Group group = new Group();
        group.setId(groupResult.id);
        group.setName(groupResult.name);
        group.setUpdated(groupResult.updated);
        group.setAbout(groupResult.about);
        group.setPublic(TRUE.equals(groupResult.isPublic));
        return group;
    }

    private Group updateGroupFromGroupResult(Group group, GroupResult groupResult) {
        group.setName(groupResult.name);
        group.setUpdated(groupResult.updated);
        group.setAbout(groupResult.about);
        group.setPublic(TRUE.equals(groupResult.isPublic));
        return group;
    }

    private GroupContact createGroupContactFromGroupContactResult(GroupContactResult groupContactResult) {
        GroupContact groupContact = new GroupContact();
        groupContact.setId(groupContactResult.id);
        groupContact.setContactId(groupContactResult.from);
        groupContact.setGroupId(groupContactResult.to);
        groupContact.setContactName(groupContactResult.phone.name);
        groupContact.setUpdated(groupContactResult.updated);
        return groupContact;
    }

    private GroupInvite transformGroupInviteResult(GroupInviteResult result) {
        GroupInvite groupInvite = new GroupInvite();
        groupInvite.setId(result.id);
        groupInvite.setGroup(result.group);
        groupInvite.setName(result.name);
        groupInvite.setUpdated(result.updated);
        return groupInvite;
    }

    private GroupMessage transformGroupMessageResult(GroupMessageResult result) {
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setId(result.id);
        groupMessage.setFrom(result.from);
        groupMessage.setTo(result.to);
        groupMessage.setText(result.text);
        groupMessage.setTime(result.created);
        groupMessage.setUpdated(result.updated);
        groupMessage.setAttachment(result.attachment);
        return groupMessage;
    }

    interface CreateTransformer<T, R> {
        T transform(R result);
    }

    interface UpdateTransformer<T, R> {
        T transform(T existing, R result);
    }
}
