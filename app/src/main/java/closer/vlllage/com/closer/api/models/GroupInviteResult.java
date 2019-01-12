package closer.vlllage.com.closer.api.models;

import closer.vlllage.com.closer.store.models.GroupInvite;

public class GroupInviteResult extends ModelResult {
    public String name;
    public String group;

    public static GroupInvite from(GroupInviteResult result) {
        GroupInvite groupInvite = new GroupInvite();
        groupInvite.setId(result.id);
        groupInvite.setGroup(result.group);
        groupInvite.setName(result.name);
        groupInvite.setUpdated(result.updated);
        return groupInvite;
    }
}
