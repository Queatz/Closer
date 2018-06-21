package closer.vlllage.com.closer.store.models;

import java.util.Date;

import io.objectbox.annotation.Entity;

@Entity
public class PhoneMessage extends BaseObject {
    private String to;
    private String from;
    private Date time;
    private String text;
    private String attachment;

    public String getTo() {
        return to;
    }

    public PhoneMessage setTo(String to) {
        this.to = to;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public PhoneMessage setFrom(String from) {
        this.from = from;
        return this;
    }

    public Date getTime() {
        return time;
    }

    public PhoneMessage setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getText() {
        return text;
    }

    public PhoneMessage setText(String text) {
        this.text = text;
        return this;
    }

    public String getAttachment() {
        return attachment;
    }

    public PhoneMessage setAttachment(String attachment) {
        this.attachment = attachment;
        return this;
    }
}