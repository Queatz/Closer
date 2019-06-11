package closer.vlllage.com.closer.handler.event

import closer.vlllage.com.closer.handler.bubble.BubbleHandler
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Event_
import io.objectbox.android.AndroidScheduler
import java.util.*

class EventBubbleHandler constructor(private val on: On) {

    private val visibleEvents = HashSet<String>()

    fun attach() {
        on<DisposableHandler>().add(on<StoreHandler>().store.box(Event::class).query()
                .greater(Event_.endsAt, Calendar.getInstance(TimeZone.getTimeZone("UTC")).time)
                .notEqual(Event_.cancelled, true)
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer { events ->
                    on<BubbleHandler>().remove { mapBubble -> mapBubble.type == BubbleType.EVENT && !visibleEvents.contains((mapBubble.tag as Event).id) }
                    for (event in events) {
                        if (!visibleEvents.contains(event.id)) {
                            on<BubbleHandler>().add(on<EventHandler>().eventBubbleFrom(event))
                        }
                    }

                    visibleEvents.clear()

                    for (event in events) {
                        if (event.id == null || event.groupId == null) {
                            continue
                        }
                        visibleEvents.add(event.id!!)
                    }
                })
    }
}
