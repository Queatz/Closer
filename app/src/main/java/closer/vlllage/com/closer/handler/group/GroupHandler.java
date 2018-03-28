package closer.vlllage.com.closer.handler.group;

import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.AlertHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
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

        groupName.setOnClickListener(view -> {
            $(AlertHandler.class).makeAlert()
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.set_group_name))
                    .setLayoutResId(R.layout.set_name_modal)
                    .setTextView(R.id.input, value -> {
                        if (group == null) {
                            return;
                        }

                        group.setName(value);
                        groupName.setText(value);
                        $(StoreHandler.class).getStore().box(Group.class).put(group);
                    })
                    .setOnAfterViewCreated(alertView -> {
                        TextView input = alertView.findViewById(R.id.input);

                        if (group != null && group.getName() != null) {
                            input.setText(group.getName());
                        }
                    })
                    .show();
        });
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
