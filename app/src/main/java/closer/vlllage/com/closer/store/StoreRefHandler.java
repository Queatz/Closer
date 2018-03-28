package closer.vlllage.com.closer.store;

import closer.vlllage.com.closer.handler.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class StoreRefHandler extends PoolMember {

    private Store store;

    @Override
    public void onPoolInit() {
        this.store = new Store($(ApplicationHandler.class).getApp());
    }

    @Override
    protected void onPoolEnd() {
        store.close();
    }

    public Store get() {
        return store;
    }
}
