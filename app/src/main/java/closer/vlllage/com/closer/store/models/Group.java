package closer.vlllage.com.closer.store.models;

import io.objectbox.annotation.Entity;

@Entity
public class Group extends BaseObject {
    private String name;

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }
}
