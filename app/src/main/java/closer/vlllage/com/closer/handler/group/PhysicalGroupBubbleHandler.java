package closer.vlllage.com.closer.handler.group;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import closer.vlllage.com.closer.handler.bubble.BubbleHandler;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.map.MapZoomHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.DataSubscription;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;

public class PhysicalGroupBubbleHandler extends PoolMember {
    private final Set<String> visiblePublicGroups = new HashSet<>();
    private DataSubscription physicalGroupSubscription;

    public void attach() {
        $(DisposableHandler.class).add($(MapZoomHandler.class).onZoomGreaterThanChanged(15).subscribe(
                zoomIsGreaterThan15 -> {
                    if (zoomIsGreaterThan15) {
                        $(DisposableHandler.class).add(getNewPhysicalGroupObservable());
                    } else {
                        visiblePublicGroups.clear();
                        clearBubbles();
                        if (physicalGroupSubscription != null) {
                            $(DisposableHandler.class).dispose(physicalGroupSubscription);
                            physicalGroupSubscription = null;
                        }
                    }

                }, Throwable::printStackTrace
        ));
    }

    private DataSubscription getNewPhysicalGroupObservable() {
        if (physicalGroupSubscription != null) {
            $(DisposableHandler.class).dispose(physicalGroupSubscription);
        }

        Date oneHourAgo = new Date();
        oneHourAgo.setTime(oneHourAgo.getTime() - HOUR_IN_MILLIS);

        Date oneMonthAgo = new Date();
        oneMonthAgo.setTime(oneMonthAgo.getTime() - 30 * DAY_IN_MILLIS);

        physicalGroupSubscription = $(StoreHandler.class).getStore().box(Group.class).query()
                .equal(Group_.physical, true)
                .and()
                .greater(Group_.updated, oneHourAgo)
                .or()
                .equal(Group_.hub, true)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer(groups -> {
                    clearBubbles();
                    for (Group group : groups) {
                        if (!visiblePublicGroups.contains(group.getId())) {
                            MapBubble mapBubble = $(PhysicalGroupHandler.class).physicalGroupBubbleFrom(group);

                            if (mapBubble != null) {
                                $(BubbleHandler.class).add(mapBubble);
                            }
                        }
                    }

                    visiblePublicGroups.clear();
                    for (Group group : groups) {
                        if (group.getId() == null) {
                            continue;
                        }
                        visiblePublicGroups.add(group.getId());
                    }
                });

        return physicalGroupSubscription;
    }

    private void clearBubbles() {
        $(BubbleHandler.class).remove(mapBubble -> mapBubble.getType() == BubbleType.PHYSICAL_GROUP &&
                !visiblePublicGroups.contains(((Group) mapBubble.getTag()).getId()));
    }
}
