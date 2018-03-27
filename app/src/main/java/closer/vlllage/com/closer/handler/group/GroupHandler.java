package closer.vlllage.com.closer.handler.group;

import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.AlertHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class GroupHandler extends PoolMember {

    private TextView groupName;
    private TextView peopleInGroup;

    public void attach(TextView groupName, TextView peopleInGroup) {
        this.groupName = groupName;
        this.peopleInGroup = peopleInGroup;
    }

    public void setGroupById(String groupId) {
        groupName.setText("888 Friends");
        peopleInGroup.setText("Jacob, Bun, Mai, Phoung (invited), Le My, Stella");

        groupName.setOnClickListener(view -> {
            $(AlertHandler.class).makeAlert()
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.set_group_name))
                    .setLayoutResId(R.layout.set_name_modal)
                    .setOnAfterViewCreated(alertView -> {
                        TextView input = alertView.findViewById(R.id.input);
                        input.setText("888 Friends");
                    })
                    .show();
        });
    }
}
