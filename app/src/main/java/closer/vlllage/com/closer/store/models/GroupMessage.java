package closer.vlllage.com.closer.store.models;

import java.util.Date;

import io.objectbox.annotation.Entity;

@Entity
public class GroupMessage extends BaseObject {
    private String to;
    private String from;
    private Date time;
    private String text;
    private String attachment;
    private Double latitude;
    private Double longitude;

    public String getTo() {
        return to;
    }

    public GroupMessage setTo(String to) {
        this.to = to;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public GroupMessage setFrom(String from) {
        this.from = from;
        return this;
    }

    public Date getTime() {
        return time;
    }

    public GroupMessage setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getText() {
        return text;
    }

    public GroupMessage setText(String text) {
        this.text = text;
        return this;
    }

    public String getAttachment() {
        return attachment;
    }

    public GroupMessage setAttachment(String attachment) {
        this.attachment = attachment;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public GroupMessage setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Double getLongitude() {
        return longitude;
    }

    public GroupMessage setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }
}
