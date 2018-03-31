package closer.vlllage.com.closer.handler.group;

import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;

public class GroupHandler extends PoolMember {

    private TextView groupName;
    private TextView peopleInGroup;
    private Group group;

    public void attach(TextView groupName, TextView peopleInGroup) {
        this.groupName = groupName;
        this.peopleInGroup = peopleInGroup;
    }

    public void setGroupById(String groupId) {
        if (groupId != null) {
            setGroup($(StoreHandler.class).getStore().box(Group.class).query()
                    .equal(Group_.id, groupId)
                    .build().findFirst());
        }

        peopleInGroup.setText("Jacob, Bun, Mai, Phoung (invited), Le My, Stella");
    }

    private void setGroup(Group group) {
        this.group = group;

        if (group.getName() != null) {
            groupName.setText(group.getName());
        } else {
            groupName.setText(R.string.not_found);
        }
    }
}
