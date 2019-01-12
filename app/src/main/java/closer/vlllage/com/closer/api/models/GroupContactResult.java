package closer.vlllage.com.closer.api.models;

import closer.vlllage.com.closer.store.models.GroupContact;

public class GroupContactResult extends ModelResult {
    public String to;
    public String from;
    public PhoneResult phone;

    public static GroupContact from(GroupContactResult groupContactResult) {
        GroupContact groupContact = new GroupContact();
        groupContact.setId(groupContactResult.id);
        groupContact.setContactId(groupContactResult.from);
        groupContact.setGroupId(groupContactResult.to);
        groupContact.setContactName(groupContactResult.phone.name);
        groupContact.setContactActive(groupContactResult.phone.updated);
        groupContact.setUpdated(groupContactResult.updated);
        return groupContact;
    }
}
