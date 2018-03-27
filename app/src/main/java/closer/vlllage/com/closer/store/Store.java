package closer.vlllage.com.closer.store;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class Store {

    private static final String REALM_FILE = "closer.realm";
    private final Realm realm;

    public Store() {
        RealmConfiguration config = new RealmConfiguration.Builder().name(REALM_FILE).build();
        Realm.setDefaultConfiguration(config);
        realm = Realm.getDefaultInstance();
    }

    public void close() {
        realm.close();
    }

    public Realm getRealm() {
        return realm;
    }
}
