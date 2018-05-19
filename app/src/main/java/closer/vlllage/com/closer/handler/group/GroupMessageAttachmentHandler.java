package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Date;

import closer.vlllage.com.closer.handler.helpers.JsonHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Suggestion;

public class GroupMessageAttachmentHandler extends PoolMember {

    public boolean shareLocation(@NonNull LatLng latLng, @NonNull Group group) {
        Suggestion suggestion = new Suggestion();
        suggestion.setLatitude(latLng.latitude);
        suggestion.setLongitude(latLng.longitude);
        return shareSuggestion(suggestion, group);
    }

    public boolean shareSuggestion(@NonNull Suggestion suggestion, @NonNull Group group) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("suggestion", $(JsonHandler.class).toJsonTree(suggestion));

        saveMessageWithAttachment(group.getId(), jsonObject);

        return true;
    }

    public boolean shareEvent(@NonNull Event event, @NonNull Group group) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("event", $(JsonHandler.class).toJsonTree(event));

        saveMessageWithAttachment(group.getId(), jsonObject);

        return true;
    }

    public boolean sharePhoto(@NonNull String photoUrl, @NonNull String groupId) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("photo", new JsonPrimitive(photoUrl));
        saveMessageWithAttachment(groupId, jsonObject);
        return true;
    }

    private void saveMessageWithAttachment(String groupId, JsonObject jsonObject) {
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setAttachment($(JsonHandler.class).to(jsonObject));
        groupMessage.setTo(groupId);
        groupMessage.setFrom($(PersistenceHandler.class).getPhoneId());
        groupMessage.setTime(new Date());
        $(StoreHandler.class).getStore().box(GroupMessage.class).put(groupMessage);
        $(SyncHandler.class).sync(groupMessage);
    }

    @Nullable
    private GroupContact getGroupContactForGroup(@NonNull Group group) {
        if ($(PersistenceHandler.class).getPhoneId() == null) {
            return null;
        }

        return $(StoreHandler.class).getStore().box(GroupContact.class).query()
                .equal(GroupContact_.contactId, $(PersistenceHandler.class).getPhoneId())
                .equal(GroupContact_.groupId, group.getId())
                .build().findFirst();
    }
}