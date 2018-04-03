package closer.vlllage.com.closer.handler;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.BaseObject;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Suggestion;

public class SyncHandler extends PoolMember {
    public void syncAll() {

    }

    public void sync(BaseObject obj) {
        obj.setLocalOnly(true);
        send(obj);
        syncAll();
    }

    private void send(final BaseObject obj) {
        if (obj instanceof Group) {
            sendCreateGroup((Group) obj);
        } else if (obj instanceof Suggestion) {
            sendCreateSuggestion((Suggestion) obj);
        } else if (obj instanceof GroupMessage) {
            sendCreateGroupMessage((GroupMessage) obj);
        } else {
            throw new RuntimeException("Unknown object type for sync: " + obj);
        }
    }

    private void sendCreateSuggestion(Suggestion suggestion) {
        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class).addSuggestion(
                suggestion.getName(),
                new LatLng(suggestion.getLatitude(), suggestion.getLongitude())
        ).subscribe(createResult -> {
                    if (createResult.success) {
                        suggestion.setId(createResult.id);
                        suggestion.setLocalOnly(false);
                        $(StoreHandler.class).getStore().box(Suggestion.class).put(suggestion);
                    } else {
                        $(DefaultAlerts.class).syncError();
                    }
                }, error -> $(DefaultAlerts.class).syncError()));
    }

    private void sendCreateGroup(Group group) {
        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class).createGroup(
                group.getName()
        ).subscribe(createResult -> {
            if (createResult.success) {
                group.setId(createResult.id);
                group.setLocalOnly(false);
                $(StoreHandler.class).getStore().box(Group.class).put(group);
            } else {
                $(DefaultAlerts.class).syncError();
            }
        }, error -> $(DefaultAlerts.class).syncError()));
    }

    private void sendCreateGroupMessage(GroupMessage groupMessage) {
        $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class).sendGroupMessage(
                groupMessage.getGroupId(),
                groupMessage.getText(),
                groupMessage.getAttachment()
        ).subscribe(createResult -> {
            if (createResult.success) {
                groupMessage.setId(createResult.id);
                groupMessage.setLocalOnly(false);
                $(StoreHandler.class).getStore().box(GroupMessage.class).put(groupMessage);
            } else {
                $(DefaultAlerts.class).syncError();
            }
        }, error -> $(DefaultAlerts.class).syncError()));
    }
}
