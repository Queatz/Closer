package closer.vlllage.com.closer.api.models;

import closer.vlllage.com.closer.store.models.GroupMember;

public class GroupMemberResult extends ModelResult {
    public String from;
    public String to;
    public boolean muted;
    public boolean subscribed;

    public static GroupMember from(GroupMemberResult groupMemberResult) {
        GroupMember groupMember = new GroupMember();
        groupMember.setId(groupMemberResult.id);
        updateFrom(groupMember, groupMemberResult);
        return groupMember;
    }

    public static GroupMember updateFrom(GroupMember groupMember, GroupMemberResult groupMemberResult) {
        groupMember.setGroup(groupMemberResult.to);
        groupMember.setPhone(groupMemberResult.from);
        groupMember.setMuted(groupMemberResult.muted);
        groupMember.setSubscribed(groupMemberResult.subscribed);
        return groupMember;
    }
}
