package closer.vlllage.com.closer.handler.helpers;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Group;

public class GroupColorHandler extends PoolMember {

    public int getColor(Group group) {
        if (group.hasEvent()) {
            return $(ResourcesHandler.class).getResources().getColor(R.color.red);
        } else if (group.isPhysical()) {
            return $(ResourcesHandler.class).getResources().getColor(R.color.purple);
        } else if (group.isPublic()) {
            return $(ResourcesHandler.class).getResources().getColor(R.color.green);
        } else {
            return $(ResourcesHandler.class).getResources().getColor(R.color.colorPrimary);
        }
    }

    public int getLightColor(Group group) {
        if (group.hasEvent()) {
            return $(ResourcesHandler.class).getResources().getColor(R.color.redLight);
        } else if (group.isPhysical()) {
            return $(ResourcesHandler.class).getResources().getColor(R.color.purpleLight);
        } else if (group.isPublic()) {
            return $(ResourcesHandler.class).getResources().getColor(R.color.greenLight);
        } else {
            return $(ResourcesHandler.class).getResources().getColor(R.color.colorPrimaryLight);
        }
    }
}
