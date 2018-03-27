package closer.vlllage.com.closer.store;

import closer.vlllage.com.closer.handler.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import io.realm.Realm;

public class StoreHandler extends PoolMember {

    private Store store;

    @Override
    protected void onPoolInit() {
        Realm.init($(ApplicationHandler.class).getApp());
        store = new Store();
    }

    @Override
    protected void onPoolEnd() {
        store.close();
    }

    public Store getStore() {
        return store;
    }
}
