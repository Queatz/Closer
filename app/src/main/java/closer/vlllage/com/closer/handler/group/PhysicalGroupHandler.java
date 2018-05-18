package closer.vlllage.com.closer.handler.group;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;

public class PhysicalGroupHandler extends PoolMember {

    public void createPhysicalGroup(LatLng latLng) {
        Group group = $(StoreHandler.class).create(Group.class);
        group.setName("");
        group.setAbout("");
        group.setPublic(true);
        group.setPhysical(true);
        group.setLatitude(latLng.latitude);
        group.setLongitude(latLng.longitude);
        $(StoreHandler.class).getStore().box(Group.class).put(group);
        $(SyncHandler.class).sync(group, this::openGroup);
    }

    private void openGroup(String groupId) {
        $(GroupActivityTransitionHandler.class).showGroupMessages(null, groupId);
    }
}
