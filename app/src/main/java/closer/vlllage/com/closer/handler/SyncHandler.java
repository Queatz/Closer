package closer.vlllage.com.closer.handler;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.BaseObject;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.store.models.Suggestion;
import closer.vlllage.com.closer.store.models.Suggestion_;
import io.objectbox.Property;
import io.objectbox.android.AndroidScheduler;

public class SyncHandler extends PoolMember {
    public void syncAll() {
        syncAll(Suggestion.class, Suggestion_.localOnly);
        syncAll(Group.class, Group_.localOnly);
        syncAll(GroupMessage.class, GroupMessage_.localOnly);
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
                .observer(this::syncAll);
    }

    private void syncAll(List<? extends BaseObject> objs) {
        for (BaseObject obj : objs) {
            sync(obj);
        }
    }

    private <T extends BaseObject> void send(final T obj, OnSyncResult onSyncResult) {
        if (obj instanceof Group) {
            sendCreateGroup((Group) obj,onSyncResult);
        } else if (obj instanceof Suggestion) {
            sendCreateSuggestion((Suggestion) obj,onSyncResult);
        } else if (obj instanceof GroupMessage) {
            sendCreateGroupMessage((GroupMessage) obj,onSyncResult);
        } else {
            throw new RuntimeException("Unknown object type for sync: " + obj);
        }
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
                    } else {
                        $(DefaultAlerts.class).syncError();
                    }
                }, error -> $(DefaultAlerts.class).syncError()));
    }

    private void sendCreateGroup(Group group, OnSyncResult onSyncResult) {
        group.setLocalOnly(true);
        $(StoreHandler.class).getStore().box(Group.class).put(group);

        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class).createGroup(
                group.getName()
        ).subscribe(createResult -> {
            if (createResult.success) {
                group.setId(createResult.id);
                group.setLocalOnly(false);
                $(StoreHandler.class).getStore().box(Group.class).put(group);
                if (onSyncResult != null) {
                    onSyncResult.onSync(createResult.id);
                }
            } else {
                $(DefaultAlerts.class).syncError();
            }
        }, error -> $(DefaultAlerts.class).syncError()));
    }

    private void sendCreateGroupMessage(GroupMessage groupMessage, OnSyncResult onSyncResult) {
        groupMessage.setLocalOnly(true);
        $(StoreHandler.class).getStore().box(GroupMessage.class).put(groupMessage);

        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class).sendGroupMessage(
                groupMessage.getGroupId(),
                groupMessage.getText(),
                groupMessage.getAttachment()
        ).subscribe(createResult -> {
            if (createResult.success) {
                groupMessage.setId(createResult.id);
                groupMessage.setLocalOnly(false);
                $(StoreHandler.class).getStore().box(GroupMessage.class).put(groupMessage);
                if (onSyncResult != null) {
                    onSyncResult.onSync(createResult.id);
                }
            } else {
                $(DefaultAlerts.class).syncError();
            }
        }, error -> $(DefaultAlerts.class).syncError()));
    }

    public interface OnSyncResult {
        void onSync(String id);
    }
}
