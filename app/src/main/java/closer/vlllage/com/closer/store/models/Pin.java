package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;

@Entity
public class Pin extends BaseObject {
    private String from;
    private String to;

    public String getFrom() {
        return from;
    }

    public Pin setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Pin setTo(String to) {
        this.to = to;
        return this;
    }
}
