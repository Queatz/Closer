package closer.vlllage.com.closer.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import closer.vlllage.com.closer.store.models.Group;

import static java.lang.Boolean.TRUE;

public class GroupResult extends ModelResult {
    public String name;
    public String about;
    @SerializedName("public") public Boolean isPublic;
    public Boolean physical;
    public Boolean hub;
    public String eventId;
    public List<Double> geo;

    public static Group from(GroupResult groupResult) {
        Group group = new Group();
        group.setId(groupResult.id);
        updateFrom(group, groupResult);
        return group;
    }

    public static Group updateFrom(Group group, GroupResult groupResult) {
        group.setName(groupResult.name);
        group.setUpdated(groupResult.updated);
        group.setAbout(groupResult.about);
        group.setPublic(TRUE.equals(groupResult.isPublic));
        group.setPhysical(TRUE.equals(groupResult.physical));
        group.setHub(TRUE.equals(groupResult.hub));
        group.setEventId(groupResult.eventId);

        if (groupResult.geo != null && !groupResult.geo.isEmpty()) {
            group.setLatitude(groupResult.geo.get(0));
            group.setLongitude(groupResult.geo.get(1));
        }

        return group;
    }
}
