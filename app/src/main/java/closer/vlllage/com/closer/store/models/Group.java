package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;

@Entity
public class Group extends BaseObject {
    private String name;
    private String about;
    private boolean isPublic;
    private String eventId;
    private Double latitude;
    private Double longitude;

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public String getAbout() {
        return about;
    }

    public Group setAbout(String about) {
        this.about = about;
        return this;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public Group setPublic(boolean aPublic) {
        isPublic = aPublic;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Group setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Group setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public Group setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public boolean hasEvent() {
        return eventId != null;
    }
}
