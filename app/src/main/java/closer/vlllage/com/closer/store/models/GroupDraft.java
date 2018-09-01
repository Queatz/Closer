package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class GroupDraft {
    @Id private long objectBoxId;
    private String groupId;
    private String message;

    public long getObjectBoxId() {
        return objectBoxId;
    }

    public GroupDraft setObjectBoxId(long objectBoxId) {
        this.objectBoxId = objectBoxId;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public GroupDraft setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public GroupDraft setMessage(String message) {
        this.message = message;
        return this;
    }
}
