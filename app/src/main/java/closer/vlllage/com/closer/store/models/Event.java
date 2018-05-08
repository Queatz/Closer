package closer.vlllage.com.closer.store.models;

import java.util.Date;

import io.objectbox.annotation.Entity;

@Entity
public class Event extends BaseObject {
    private String name;
    private String about;
    private String groupId;
    private Double latitude;
    private Double longitude;
    private Date startsAt;
    private Date endsAt;
    private boolean cancelled;

    public String getName() {
        return name;
    }

    public Event setName(String name) {
        this.name = name;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Event setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Event setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public String getAbout() {
        return about;
    }

    public Event setAbout(String about) {
        this.about = about;
        return this;
    }

    public Date getStartsAt() {
        return startsAt;
    }

    public Event setStartsAt(Date startsAt) {
        this.startsAt = startsAt;
        return this;
    }

    public Date getEndsAt() {
        return endsAt;
    }

    public Event setEndsAt(Date endsAt) {
        this.endsAt = endsAt;
        return this;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Event setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public Event setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public boolean hasGroup() {
        return groupId != null;
    }
}
