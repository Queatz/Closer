package closer.vlllage.com.closer.handler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

import java.util.Date;

import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Suggestion;

public class GroupMessageSuggestionsHandler extends PoolMember {

    public boolean shareLocation(@NonNull LatLng latLng, @NonNull Group group) {
        Suggestion suggestion = new Suggestion();
        suggestion.setLatitude(latLng.latitude);
        suggestion.setLongitude(latLng.longitude);
        return shareSuggestion(suggestion, group);
    }

    public boolean shareSuggestion(@NonNull Suggestion suggestion, @NonNull Group group) {
        GroupContact groupContact = getGroupContactForGroup(group);

        // todo remove:
        if (groupContact == null) {
            return false;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("suggestion", $(JsonHandler.class).toJsonTree(suggestion));

        saveMessageWithAttachment(groupContact, jsonObject);

        return true;
    }

    private void saveMessageWithAttachment(GroupContact groupContact, JsonObject jsonObject) {
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setAttachment($(JsonHandler.class).to(jsonObject));
        groupMessage.setGroupId(groupContact.getGroupId());
        groupMessage.setContactId(groupContact.getId());
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
