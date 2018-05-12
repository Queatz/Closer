package closer.vlllage.com.closer.store.models;

import java.util.Date;

import io.objectbox.annotation.Entity;

@Entity
public class GroupContact extends BaseObject {
    private String groupId;
    private String contactId;
    private String contactName;
    private Date contactActive;

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

    public String getContactName() {
        return contactName;
    }

    public GroupContact setContactName(String contactName) {
        this.contactName = contactName;
        return this;
    }

    public Date getContactActive() {
        return contactActive;
    }

    public GroupContact setContactActive(Date contactActive) {
        this.contactActive = contactActive;
        return this;
    }
}
