package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;

@Entity
public class GroupContact extends BaseObject {
    private String groupId;
    private String contactId;

    public String getGroupId() {
        return groupId;
    }

    public GroupContact setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getContactId() {
        return contactId;
    }

    public GroupContact setContactId(String contactId) {
        this.contactId = contactId;
        return this;
    }
}
