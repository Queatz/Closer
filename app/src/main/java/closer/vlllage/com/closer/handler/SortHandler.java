package closer.vlllage.com.closer.handler;

import java.util.Comparator;

import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;

public class SortHandler extends PoolMember {

    public Comparator<Group> sortGroups() {
        return (group, groupOther) -> group.getUpdated() == null || groupOther.getUpdated() == null ? 0 : groupOther.getUpdated().compareTo(group.getUpdated());
    }

    public Comparator<GroupMessage> sortGroupMessages() {
        return (groupMessage, groupMessageOther) -> groupMessage.getTime() == null || groupMessageOther.getTime() == null ? 0 : groupMessageOther.getTime().compareTo(groupMessage.getTime());
    }
}
