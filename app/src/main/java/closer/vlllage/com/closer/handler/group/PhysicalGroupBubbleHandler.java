package closer.vlllage.com.closer.handler.group;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import closer.vlllage.com.closer.handler.bubble.BubbleHandler;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;

public class PhysicalGroupBubbleHandler extends PoolMember {
    private final Set<String> visiblePublicGroups = new HashSet<>();

    public void attach() {
        Date oneHourAgo = new Date();
        oneHourAgo.setTime(oneHourAgo.getTime() - HOUR_IN_MILLIS);

        Date oneMonthAgo = new Date();
        oneMonthAgo.setTime(oneMonthAgo.getTime() - 30 * DAY_IN_MILLIS);

        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(Group.class).query()
                .equal(Group_.hub, true)
                .and()
                .greater(Group_.updated, oneMonthAgo)
                .or()
                .equal(Group_.physical, true)
                .and()
                .greater(Group_.updated, oneHourAgo)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer(groups -> {
                    $(BubbleHandler.class).remove(mapBubble -> mapBubble.getType() == BubbleType.PHYSICAL_GROUP && !visiblePublicGroups.contains(((Group) mapBubble.getTag()).getId()));
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
                }));
    }
}
