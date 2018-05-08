package closer.vlllage.com.closer.api.models;

import com.google.gson.annotations.SerializedName;

import closer.vlllage.com.closer.store.models.Group;

import static java.lang.Boolean.TRUE;

public class GroupResult extends ModelResult {
    public String name;
    public String about;
    @SerializedName("public") public Boolean isPublic;
    public String eventId;

    public static Group from(GroupResult groupResult) {
        Group group = new Group();
        group.setId(groupResult.id);
        group.setName(groupResult.name);
        group.setUpdated(groupResult.updated);
        group.setAbout(groupResult.about);
        group.setPublic(TRUE.equals(groupResult.isPublic));
        group.setEventId(groupResult.eventId);
        return group;
    }
}
