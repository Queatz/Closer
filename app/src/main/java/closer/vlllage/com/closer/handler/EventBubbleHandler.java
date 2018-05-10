package closer.vlllage.com.closer.handler;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Event_;
import io.objectbox.android.AndroidScheduler;

public class EventBubbleHandler extends PoolMember {

    private final Set<String> visibleEvents = new HashSet<>();

    public void attach() {
        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(Event.class).query()
                .greater(Event_.endsAt, Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime())
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer(events -> {
                    $(BubbleHandler.class).remove(mapBubble -> mapBubble.getType() == BubbleType.EVENT && !visibleEvents.contains(((Event) mapBubble.getTag()).getId()));
                    for (Event event : events) {
                        if (!visibleEvents.contains(event.getId())) {
                            $(BubbleHandler.class).add($(EventHandler.class).eventBubbleFrom(event));
                        }
                    }

                    visibleEvents.clear();
                    for (Event event : events) {
                        if (event.getId() == null || event.getGroupId() == null) {
                            continue;
                        }
                        visibleEvents.add(event.getId());
                    }
                }));
    }
}
