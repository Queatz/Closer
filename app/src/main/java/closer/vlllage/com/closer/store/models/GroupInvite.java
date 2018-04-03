package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;

@Entity
public class GroupInvite extends BaseObject {
    private String group;
    private String name;

    public String getName() {
        return name;
    }

    public GroupInvite setName(String name) {
        this.name = name;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public GroupInvite setGroup(String group) {
        this.group = group;
        return this;
    }
}
