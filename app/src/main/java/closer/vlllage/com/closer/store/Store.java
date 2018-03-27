package closer.vlllage.com.closer.store;

import android.app.Application;

import closer.vlllage.com.closer.store.models.BaseObject;
import closer.vlllage.com.closer.store.models.MyObjectBox;
import io.objectbox.Box;
import io.objectbox.BoxStore;

public class Store {

    private final BoxStore boxStore;

    Store(Application app) {
        boxStore = MyObjectBox.builder().androidContext(app).build();
    }

    public void close() {
        boxStore.close();
    }

    public <T extends BaseObject> Box<T> box(Class<T> clazz) {
        return boxStore.boxFor(clazz);
    }
}
