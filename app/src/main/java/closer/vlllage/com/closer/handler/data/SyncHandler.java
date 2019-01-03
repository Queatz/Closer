package closer.vlllage.com.closer.handler.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import closer.vlllage.com.closer.api.models.CreateResult;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.HttpEncode;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.BaseObject;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Event_;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupAction;
import closer.vlllage.com.closer.store.models.GroupAction_;
import closer.vlllage.com.closer.store.models.GroupMember;
import closer.vlllage.com.closer.store.models.GroupMember_;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.store.models.Suggestion;
import closer.vlllage.com.closer.store.models.Suggestion_;
import io.objectbox.Property;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.DataObserver;
import io.reactivex.Single;

public class SyncHandler extends PoolMember {
    public void syncAll() {
        syncAll(Suggestion.class, Suggestion_.localOnly);
        syncAll(Group.class, Group_.localOnly);
        syncAll(GroupMessage.class, GroupMessage_.localOnly);
        syncAll(Event.class, Event_.localOnly);
        syncAll(GroupAction.class, GroupAction_.localOnly);
        syncAll(GroupMember.class, GroupMember_.localOnly);
    }

    public <T extends BaseObject> void sync(T obj) {
        sync(obj, null);
    }

    public <T extends BaseObject> void sync(T obj, OnSyncResult onSyncResult) {
        send(obj, onSyncResult);
    }

    private void syncAll(Class<? extends BaseObject> clazz, Property localOnlyProperty) {
        $(StoreHandler.class).getStore().box(clazz).query()
                .equal(localOnlyProperty, true)
                .build().subscribe().single().on(AndroidScheduler.mainThread())
                .observer((DataObserver<List<? extends BaseObject>>) this::syncAll);
    }

    private void syncAll(List<? extends BaseObject> objs) {
        for (BaseObject obj : objs) {
            sync(obj);
        }
    }

    private <T extends BaseObject> void send(final T obj, OnSyncResult onSyncResult) {
        if (obj instanceof Group) {
            sendCreateGroup((Group) obj, onSyncResult);
        } else if (obj instanceof Suggestion) {
            sendCreateSuggestion((Suggestion) obj, onSyncResult);
        } else if (obj instanceof GroupMessage) {
            sendCreateGroupMessage((GroupMessage) obj, onSyncResult);
        } else if (obj instanceof Event) {
            sendCreateEvent((Event) obj, onSyncResult);
        } else if (obj instanceof GroupAction) {
            sendCreateGroupAction((GroupAction) obj, onSyncResult);
        } else if (obj instanceof GroupMember) {
            sendUpdateGroupMember((GroupMember) obj, onSyncResult);
        } else {
            throw new RuntimeException("Unknown object type for sync: " + obj);
        }
    }

    private void sendCreateGroupAction(GroupAction groupAction, OnSyncResult onSyncResult) {
        groupAction.setLocalOnly(true);
        $(StoreHandler.class).getStore().box(GroupAction.class).put(groupAction);

        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class).createGroupAction(
                groupAction.getGroup(),
                groupAction.getName(),
                groupAction.getIntent()
        ).subscribe(createResult -> {
            if (createResult.success) {
                groupAction.setId(createResult.id);
                groupAction.setLocalOnly(false);
                $(StoreHandler.class).getStore().box(GroupAction.class).put(groupAction);
                if (onSyncResult != null) {
                    onSyncResult.onSync(createResult.id);
                }
            }
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    private void sendUpdateGroupMember(GroupMember groupMember, OnSyncResult onSyncResult) {
        groupMember.setLocalOnly(true);
        $(StoreHandler.class).getStore().box(GroupMember.class).put(groupMember);

        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class).updateGroupMember(
                groupMember.getGroup(),
                groupMember.isMuted(),
                groupMember.isSubscribed()
        ).subscribe(createResult -> {
            if (createResult.success) {
                groupMember.setId(createResult.id);
                groupMember.setLocalOnly(false);
                $(StoreHandler.class).getStore().box(GroupMember.class).put(groupMember);
                if (onSyncResult != null) {
                    onSyncResult.onSync(createResult.id);
                }
            }
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    private void sendCreateEvent(Event event, OnSyncResult onSyncResult) {
        event.setLocalOnly(true);
        $(StoreHandler.class).getStore().box(Event.class).put(event);

        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class).createEvent(
                event.getName(),
                event.getAbout(),
                event.isPublic(),
                new LatLng(event.getLatitude(), event.getLongitude()),
                event.getStartsAt(),
                event.getEndsAt()
        ).subscribe(createResult -> {
            if (createResult.success) {
                event.setId(createResult.id);
                event.setLocalOnly(false);
                $(StoreHandler.class).getStore().box(Event.class).put(event);
                if (onSyncResult != null) {
                    onSyncResult.onSync(createResult.id);
                }
            }
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    private void sendCreateSuggestion(Suggestion suggestion, OnSyncResult onSyncResult) {
        suggestion.setLocalOnly(true);
        $(StoreHandler.class).getStore().box(Suggestion.class).put(suggestion);

        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class).addSuggestion(
                suggestion.getName(),
                new LatLng(suggestion.getLatitude(), suggestion.getLongitude())
        ).subscribe(createResult -> {
                    if (createResult.success) {
                        suggestion.setId(createResult.id);
                        suggestion.setLocalOnly(false);
                        $(StoreHandler.class).getStore().box(Suggestion.class).put(suggestion);
                        if (onSyncResult != null) {
                            onSyncResult.onSync(createResult.id);
                        }
                    }
                }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    private void sendCreateGroup(Group group, OnSyncResult onSyncResult) {
        group.setLocalOnly(true);
        $(StoreHandler.class).getStore().box(Group.class).put(group);

        Single<CreateResult> createApiRequest;

        if (group.isPhysical()) {
            createApiRequest = $(ApiHandler.class).createPhysicalGroup(new LatLng(
                    group.getLatitude(),
                    group.getLongitude()
            ));
        } else if (group.isPublic()) {
            createApiRequest = $(ApiHandler.class).createPublicGroup(group.getName(), group.getAbout(), new LatLng(
                    group.getLatitude(),
                    group.getLongitude()
            ));
        } else {
            createApiRequest = $(ApiHandler.class).createGroup(group.getName());
        }

        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add(createApiRequest.subscribe(createResult -> {
            if (createResult.success) {
                group.setId(createResult.id);
                group.setLocalOnly(false);
                $(StoreHandler.class).getStore().box(Group.class).put(group);
                if (onSyncResult != null) {
                    onSyncResult.onSync(createResult.id);
                }
            }
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    private void sendCreateGroupMessage(GroupMessage groupMessage, OnSyncResult onSyncResult) {
        groupMessage.setLocalOnly(true);
        $(StoreHandler.class).getStore().box(GroupMessage.class).put(groupMessage);

        Single<CreateResult> apiCall;

        if (groupMessage.getLatitude() != null && groupMessage.getLongitude() != null) {
            apiCall = $(ApiHandler.class).sendAreaMessage(
                    new LatLng(groupMessage.getLatitude(), groupMessage.getLongitude()),
                    groupMessage.getText(),
                    $(HttpEncode.class).encode(groupMessage.getAttachment())
            );
        } else {
            apiCall = $(ApiHandler.class).sendGroupMessage(
                    groupMessage.getTo(),
                    groupMessage.getText(),
                    $(HttpEncode.class).encode(groupMessage.getAttachment())
            );
        }

        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add(apiCall.subscribe(createResult -> {
            if (createResult.success) {
                groupMessage.setId(createResult.id);
                groupMessage.setLocalOnly(false);
                $(StoreHandler.class).getStore().box(GroupMessage.class).put(groupMessage);
                if (onSyncResult != null) {
                    onSyncResult.onSync(createResult.id);
                }
            }
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    public interface OnSyncResult {
        void onSync(String id);
    }
}
