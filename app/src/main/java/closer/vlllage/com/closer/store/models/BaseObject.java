package closer.vlllage.com.closer.store.models;

public class BaseObject {
    private String id;
    private boolean localOnly;

    public String getId() {
        return id;
    }

    public BaseObject setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isLocalOnly() {
        return localOnly;
    }

    public BaseObject setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }
}
