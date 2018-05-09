package closer.vlllage.com.closer.api.models;

import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.store.models.Event;

public class EventResult extends ModelResult {
    public List<Double> geo;
    public String name;
    public String about;
    public Date startsAt;
    public Date endsAt;
    public boolean cancelled;
    public String groupId;

    public static Event from(EventResult eventResult) {
        Event event = new Event();
        event.setId(eventResult.id);
        updateFrom(event, eventResult);
        return event;
    }

    public static Event updateFrom(Event event, EventResult eventResult) {
        event.setName(eventResult.name);
        event.setAbout(eventResult.about);
        event.setLatitude(eventResult.geo.get(0));
        event.setLongitude(eventResult.geo.get(1));
        event.setEndsAt(eventResult.endsAt);
        event.setStartsAt(eventResult.startsAt);
        event.setCancelled(eventResult.cancelled);
        event.setGroupId(eventResult.groupId);
        return event;
    }
}
