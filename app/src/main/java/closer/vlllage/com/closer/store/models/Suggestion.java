package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;

@Entity
public class Suggestion extends BaseObject {
    private String name;
    private Double latitude;
    private Double longitude;

    public String getName() {
        return name;
    }

    public Suggestion setName(String name) {
        this.name = name;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Suggestion setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Suggestion setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }
}
