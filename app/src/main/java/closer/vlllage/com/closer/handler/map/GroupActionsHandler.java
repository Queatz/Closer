package closer.vlllage.com.closer.handler.map;

import android.support.v7.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.handler.group.GroupActionAdapter;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.search.GroupActionRecyclerViewHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupAction;
import closer.vlllage.com.closer.store.models.GroupAction_;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.DataSubscription;

public class GroupActionsHandler extends PoolMember {

    private DataSubscription dataSubscription;

    public void attach(RecyclerView groupActionsRecyclerView) {
        $(GroupActionRecyclerViewHandler.class).attach(groupActionsRecyclerView, GroupActionAdapter.Layout.PHOTO);
        $(GroupActionRecyclerViewHandler.class).setOnGroupActionRepliedListener(groupAction -> $(GroupActivityTransitionHandler.class).showGroupMessages(null, groupAction.getGroup()));
    }

    private int sort(GroupAction a, GroupAction b) {
        return a == b ? 0 :
                a.getPhoto() == null && b.getPhoto() != null ? 1 :
                a.getPhoto() != null && b.getPhoto() == null ? -1 :
                a.getName().compareTo(b.getName());
    }

    public void recenter(LatLng center) {
        if (dataSubscription != null) {
            dataSubscription.cancel();
        }

        float distance = .12f;

        dataSubscription = $(StoreHandler.class).getStore().box(Group.class)
                .query()
                .between(Group_.latitude, center.latitude - distance, center.latitude + distance)
                .between(Group_.longitude, center.longitude - distance, center.longitude + distance)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer(groups -> {
                    String[] groupIds = new String[groups.size()];
                    for (int i = 0; i < groups.size(); i++) {
                        groupIds[i] = groups.get(i).getId();
                    }

                    $(StoreHandler.class).getStore().box(GroupAction.class).query()
                            .in(GroupAction_.group, groupIds)
                            .sort(this::sort)
                            .build()
                            .subscribe()
                            .single()
                            .on(AndroidScheduler.mainThread())
                            .observer(groupActions -> $(GroupActionRecyclerViewHandler.class).getAdapter().setGroupActions(groupActions));
                });
    }
}
