package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.BaseEntity;
import io.objectbox.annotation.Id;

@BaseEntity
public class BaseObject {
    @Id private long objectBoxId;
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

    public long getObjectBoxId() {
        return objectBoxId;
    }

    public void setObjectBoxId(long objectBoxId) {
        this.objectBoxId = objectBoxId;
    }
}
