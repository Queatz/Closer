package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Group extends BaseObject {
    @Id private long objectBoxId; public long getObjectBoxId() { return objectBoxId; } public void setObjectBoxId(long objectBoxId) { this.objectBoxId = objectBoxId; }

    private String name;

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }
}
