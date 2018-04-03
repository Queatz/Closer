package closer.vlllage.com.closer.handler.group;

import android.widget.TextView;

import org.greenrobot.essentials.StringUtils;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;

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

        if (group != null) {
            peopleInGroup.setText("");
            $(StoreHandler.class).getStore().box(GroupContact.class).query()
                    .equal(GroupContact_.groupId, group.getId())
// todo                    .notEqual(GroupContact_.contactId, $(PersistenceHandler.class).getPhone())
                    .build().subscribe().single().on(AndroidScheduler.mainThread())
                    .observer(groupContacts -> {
                        if (groupContacts.isEmpty()) {
                            peopleInGroup.setText("-");
                            return;
                        }

                        List<String> names = new ArrayList<>();
                        for (GroupContact groupContact : groupContacts) {
                            names.add(groupContact.getContactName());
                        }
                        peopleInGroup.setText(StringUtils.join(names, ", "));
                    });
        }
    }

    private void setGroup(Group group) {
        this.group = group;

        if (group != null && group.getName() != null) {
            groupName.setText(group.getName());
        } else {
            groupName.setText(R.string.not_found);
        }
    }

    public Group getGroup() {
        return group;
    }
}
