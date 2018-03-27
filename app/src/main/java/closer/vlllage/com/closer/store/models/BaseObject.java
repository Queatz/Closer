package closer.vlllage.com.closer.store.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

class BaseObject extends RealmObject {
    @PrimaryKey
    private String id;

    public String getId() {
        return id;
    }

    public BaseObject setId(String id) {
        this.id = id;
        return this;
    }
}
