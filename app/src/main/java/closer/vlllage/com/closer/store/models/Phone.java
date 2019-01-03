package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;

@Entity
public class Phone extends BaseObject {
    private String name;
    private String status;
    private String photo;
    private Double latitude;
    private Double longitude;

    public String getName() {
        return name;
    }

    public Phone setName(String name) {
        this.name = name;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Phone setStatus(String status) {
        this.status = status;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Phone setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Phone setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public String getPhoto() {
        return photo;
    }

    public Phone setPhoto(String photo) {
        this.photo = photo;
        return this;
    }
}
