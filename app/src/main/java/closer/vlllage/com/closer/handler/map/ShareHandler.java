package closer.vlllage.com.closer.handler.map;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.bubble.BubbleHandler;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.bubble.MapBubbleMenuItem;
import closer.vlllage.com.closer.handler.bubble.MapBubbleMenuView;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.helpers.TimerHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;

public class ShareHandler extends PoolMember {

    public void shareTo(@NonNull LatLng latLng, @NonNull OnGroupSelectedListener onGroupSelectedListener) {
        $(StoreHandler.class).getStore().box(Group.class).query()
                .notEqual(Group_.physical, true)
                .sort($(SortHandler.class).sortGroups())
                .build().subscribe().single().on(AndroidScheduler.mainThread()).observer(groups -> {
            List<MapBubbleMenuItem> groupNames = new ArrayList<>();
            for(Group group : groups) {
                groupNames.add(new MapBubbleMenuItem().setTitle(group.getName()));
            }

            MapBubble menuBubble = new MapBubble(latLng, BubbleType.MENU);
            menuBubble.setPinned(true);
            menuBubble.setOnTop(true);
            $(TimerHandler.class).postDisposable(() -> {
                $(BubbleHandler.class).add(menuBubble);
                $(MapBubbleMenuView.class).setMenuTitle(menuBubble, $(ResourcesHandler.class).getResources().getString(R.string.share_with));
                $(MapBubbleMenuView.class).getMenuAdapter(menuBubble).setMenuItems(groupNames);
                menuBubble.setOnItemClickListener(position -> {
                    onGroupSelectedListener.onGroupSelected(groups.get(position));
                });
            }, 225);
        });
    }

    public interface OnGroupSelectedListener {
        void onGroupSelected(Group group);
    }
}
