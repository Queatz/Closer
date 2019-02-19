package closer.vlllage.com.closer.handler.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import closer.vlllage.com.closer.api.models.EventResult;
import closer.vlllage.com.closer.api.models.GroupActionResult;
import closer.vlllage.com.closer.api.models.GroupContactResult;
import closer.vlllage.com.closer.api.models.GroupInviteResult;
import closer.vlllage.com.closer.api.models.GroupMessageResult;
import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.api.models.ModelResult;
import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.PinResult;
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ListEqual;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.BaseObject;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Event_;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupAction;
import closer.vlllage.com.closer.store.models.GroupAction_;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupInvite;
import closer.vlllage.com.closer.store.models.GroupInvite_;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;
import closer.vlllage.com.closer.store.models.Pin;
import closer.vlllage.com.closer.store.models.Pin_;
import io.objectbox.Box;
import io.objectbox.Property;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;
import io.objectbox.reactive.SubscriptionBuilder;

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
            )).subscribe(this::handleMessages, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
        });
    }

    public void refreshMyGroups() {
        $(LocationHandler.class).getCurrentLocation(location -> {
            $(DisposableHandler.class).add($(ApiHandler.class).myGroups(new LatLng(
                    location.getLatitude(),
                    location.getLongitude()
            )).subscribe(stateResult -> {
                handleFullListResult(stateResult.groups, Group.class, Group_.id, true, GroupResult::from, GroupResult::updateFrom);
                handleFullListResult(stateResult.groupInvites, GroupInvite.class, GroupInvite_.id, true, GroupInviteResult::from, null);
                handleGroupContacts(stateResult.groupContacts);
            }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
        });
    }

    public void refreshGroupContacts(String groupId) {
        $(DisposableHandler.class).add($(ApiHandler.class).getContacts(groupId).subscribe(this::handleGroupContacts,
                error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    public void refreshEvents(LatLng latLng) {
        $(DisposableHandler.class).add($(ApiHandler.class).getEvents(latLng).subscribe(eventResults -> {
            handleFullListResult(eventResults, Event.class, Event_.id, false, EventResult::from, EventResult::updateFrom);
        }, error -> {
            $(ConnectionErrorHandler.class).notifyConnectionError();
        }));
    }

    public void refreshPhysicalGroups(LatLng latLng) {
        $(DisposableHandler.class).add($(ApiHandler.class).getPhysicalGroups(latLng).subscribe(groupResults -> {
            handleFullListResult(groupResults, Group.class, Group_.id, false, GroupResult::from, GroupResult::updateFrom);
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));

        $(DisposableHandler.class).add($(ApiHandler.class).myMessages(latLng)
                .subscribe(this::handleMessages, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    public void refreshGroupActions(String groupId) {
        $(DisposableHandler.class).add($(ApiHandler.class).getGroupActions(groupId).subscribe(groupActionResults -> {
            QueryBuilder<GroupAction> removeQuery = $(StoreHandler.class).getStore().box(GroupAction.class).query()
                    .equal(GroupAction_.group, groupId);

            for (GroupActionResult groupActionResult : groupActionResults) {
                removeQuery.notEqual(GroupAction_.id, groupActionResult.id);
            }

            long[] removeIds = removeQuery.build().findIds();
            $(StoreHandler.class).getStore().box(GroupAction.class).remove(removeIds);

            handleFullListResult(groupActionResults, GroupAction.class, GroupAction_.id, false, GroupActionResult::from, GroupActionResult::updateFrom);
        }, error -> {
            $(ConnectionErrorHandler.class).notifyConnectionError();
        }));
    }

    public void refreshGroupActions(LatLng latLng) {
        $(DisposableHandler.class).add($(ApiHandler.class).getGroupActions(latLng).subscribe(groupActionResults -> {
            handleFullListResult(groupActionResults, GroupAction.class, GroupAction_.id, false, GroupActionResult::from, GroupActionResult::updateFrom);
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    public void refreshPins(String groupId) {
        $(DisposableHandler.class).add($(ApiHandler.class).getPins(groupId).subscribe(pinResults -> {
            List<GroupMessageResult> groupMessageResults = new ArrayList<>();

            for (PinResult pinResult : pinResults) {
                groupMessageResults.add(pinResult.message);
            }

            handleMessages(groupMessageResults);
            handleFullListResult(pinResults, Pin.class, Pin_.id, true, PinResult::from, PinResult::updateFrom);
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    public void refreshGroupMessages(String groupId) {
        $(DisposableHandler.class).add($(ApiHandler.class).getGroupMessages(groupId)
                .subscribe(this::handleMessages, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    public void refreshGroupMessage(String groupMessageId) {
        $(DisposableHandler.class).add($(ApiHandler.class).getGroupMessage(groupMessageId)
                .subscribe(groupMessageResult -> refresh(GroupMessageResult.from(groupMessageResult)),
                        error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    public void refresh(Event event) {
        refreshObject(event, Event.class, Event_.id);
    }

    public void refresh(Group group) {
        refreshObject(group, Group.class, Group_.id);
    }

    public void refresh(Phone phone) {
        refreshObject(phone, Phone.class, Phone_.id);
    }

    public void refresh(GroupMessage groupMessage) {
        refreshObject(groupMessage, GroupMessage.class, GroupMessage_.id);
    }

    public <T extends BaseObject> void refreshObject(T object, Class<T> clazz, Property idProperty) {
        ((SubscriptionBuilder<List<T>>) $(StoreHandler.class).getStore().box(clazz)
                .query()
                .equal(idProperty, object.getId())
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread()))
                .observer(results -> {
                    if (!results.isEmpty()) {
                        object.setObjectBoxId(results.get(0).getObjectBoxId());
                    }
                    $(StoreHandler.class).getStore().box(clazz).put(object);
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
                            groupMessageBox.put(GroupMessageResult.from(message));
                        } else {
                            GroupMessage existing = existingObjsMap.get(message.id);

                            if (existing != null && !$(ListEqual.class).isEqual(message.reactions, existing.getReactions())) {
                                existing.setReactions(message.reactions);
                                groupMessageBox.put(existing);
                            }
                        }
                    }
                });
    }

    <T extends BaseObject, R extends ModelResult> void handleFullListResult(
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
                    groupsToAdd.add(GroupContactResult.from(groupContactResult));
                } else {
                    existingGroupContactsMap.get(groupContactResult.id).setContactName(groupContactResult.phone.name);
                    existingGroupContactsMap.get(groupContactResult.id).setContactActive(groupContactResult.phone.updated);
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
        handleFullListResult(phoneResults, Phone.class, Phone_.id, false, PhoneResult::from, PhoneResult::updateFrom);
    }

    interface CreateTransformer<T, R> {
        T transform(R result);
    }

    interface UpdateTransformer<T, R> {
        T transform(T existing, R result);
    }
}
