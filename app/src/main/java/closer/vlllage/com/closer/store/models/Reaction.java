package closer.vlllage.com.closer.store.models;

public class Reaction extends BaseObject {
    private String reaction;
    private String from;
    private String to;

    public String getReaction() {
        return reaction;
    }

    public Reaction setReaction(String reaction) {
        this.reaction = reaction;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public Reaction setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Reaction setTo(String to) {
        this.to = to;
        return this;
    }
}
