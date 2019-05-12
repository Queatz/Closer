package closer.vlllage.com.closer.api.models;

import closer.vlllage.com.closer.store.models.GroupAction;

public class GroupActionResult extends ModelResult {
    public String group;
    public String name;
    public String intent;
    public String photo;

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
        groupAction.setPhoto(groupActionResult.photo);
        return groupAction;
    }
}
