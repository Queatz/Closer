package closer.vlllage.com.closer.handler.helpers;

import closer.vlllage.com.closer.pool.PoolMember;

public class TopHandler extends PoolMember {

    private String activeGroupId = null;

    public boolean isGroupActive(String groupId) {
        return activeGroupId != null && activeGroupId.equals(groupId);
    }

    public void setGroupActive(String groupId) {
        activeGroupId = groupId;
    }
}
