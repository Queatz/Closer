package closer.vlllage.com.closer.store;

import java.lang.reflect.InvocationTargetException;

import closer.vlllage.com.closer.handler.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.BaseObject;
import closer.vlllage.com.closer.util.PhoneUtil;

public class StoreHandler extends PoolMember {

    private Store store;

    @Override
    protected void onPoolInit() {
        store = new Store($(ApplicationHandler.class).getApp());
    }

    @Override
    protected void onPoolEnd() {
        store.close();
    }

    public Store getStore() {
        return store;
    }

    public <T extends BaseObject> T create(Class<T> clazz) {
        try {
            T baseObject = clazz.getConstructor().newInstance();
            baseObject.setId(newLocalId());
            store.box(clazz).put(baseObject);
            return baseObject;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String newLocalId() {
        return PhoneUtil.rndId();
    }
}
