package closer.vlllage.com.closer.store.models;

import java.util.Date;

import io.objectbox.annotation.Entity;

@Entity
public class GroupMessage extends BaseObject {
    private String groupId;
    private String contactId;
    private Date time;
    private String text;
    private String attachments;

    public String getGroupId() {
        return groupId;
    }

    public GroupMessage setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getContactId() {
        return contactId;
    }

    public GroupMessage setContactId(String contactId) {
        this.contactId = contactId;
        return this;
    }

    public Date getTime() {
        return time;
    }

    public GroupMessage setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getText() {
        return text;
    }

    public GroupMessage setText(String text) {
        this.text = text;
        return this;
    }

    public String getAttachments() {
        return attachments;
    }

    public GroupMessage setAttachments(String attachments) {
        this.attachments = attachments;
        return this;
    }
}
