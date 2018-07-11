package closer.vlllage.com.closer.api.models;

import com.google.gson.annotations.Expose;

import closer.vlllage.com.closer.store.models.GroupAction;

public class GroupActionResult extends ModelResult {@Expose
    public String group;
    public String name;
    public String intent;

    public static GroupAction from(GroupActionResult groupActionResult) {
        GroupAction groupAction = new GroupAction();
        groupAction.setId(groupActionResult.id);
        updateFrom(groupAction, groupActionResult);
        return groupAction;
    }

    public static GroupAction updateFrom(GroupAction groupAction, GroupActionResult groupActionResult) {
        groupAction.setName(groupActionResult.name);
        groupAction.setIntent(groupActionResult.intent);
        groupAction.setGroup(groupActionResult.group);
        return groupAction;
    }
}