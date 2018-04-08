package closer.vlllage.com.closer.store.models;

import java.util.Date;

import io.objectbox.annotation.BaseEntity;
import io.objectbox.annotation.Id;

@BaseEntity
public class BaseObject {
    @Id private long objectBoxId;
    private String id;
    private boolean localOnly;
    private Date updated;

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

    public Date getUpdated() {
        return updated;
    }

    public BaseObject setUpdated(Date updated) {
        this.updated = updated;
        return this;
    }
}
