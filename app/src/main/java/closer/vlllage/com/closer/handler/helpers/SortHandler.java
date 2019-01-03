package closer.vlllage.com.closer.handler.helpers;

import java.util.Comparator;

import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Phone;

public class SortHandler extends PoolMember {

    public Comparator<Group> sortGroups() {
        return sortGroups(true);
    }

    public Comparator<Group> sortGroups(boolean privateFirst) {
        return (group, groupOther) ->
                !$(DistanceHandler.class).isUserNearGroup(group) && $(DistanceHandler.class).isUserNearGroup(groupOther) ? 1 :
                !$(DistanceHandler.class).isUserNearGroup(groupOther) && $(DistanceHandler.class).isUserNearGroup(group) ? -1 :
                privateFirst && group.isPublic() && !groupOther.isPublic() ? 1 :
                privateFirst && groupOther.isPublic() && !group.isPublic() ? -1 :
                group.getUpdated() == null || groupOther.getUpdated() == null ? 0 :
                groupOther.getUpdated().compareTo(group.getUpdated());
    }

    public Comparator<GroupMessage> sortGroupMessages() {
        return (groupMessage, groupMessageOther) -> groupMessage.getTime() == null || groupMessageOther.getTime() == null ? 0 : groupMessageOther.getTime().compareTo(groupMessage.getTime());
    }

    public Comparator<Phone> sortPhones() {
        return (phone, phoneOther) -> phone.getUpdated() == null || phoneOther.getUpdated() == null ? 0 : phoneOther.getUpdated().compareTo(phone.getUpdated());
    }
}
