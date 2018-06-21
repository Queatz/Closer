package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;

@Entity
public class GroupAction extends BaseObject {
    private String group;
    private String name;

    public String getGroup() {
        return group;
    }

    public GroupAction setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getName() {
        return name;
    }

    public GroupAction setName(String name) {
        this.name = name;
        return this;
    }
}
