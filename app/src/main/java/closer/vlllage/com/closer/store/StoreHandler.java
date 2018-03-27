package closer.vlllage.com.closer.store;

import java.lang.reflect.InvocationTargetException;

import closer.vlllage.com.closer.handler.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.BaseObject;
import closer.vlllage.com.closer.util.PhoneUtil;
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

    public <T extends BaseObject> T create(Class<T> clazz) {
        try {
            T realmObject = clazz.getConstructor().newInstance();
            realmObject.setId(newLocalId());
            insert(realmObject);
            return realmObject;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public  <T extends BaseObject> void insert(T realmObject) {
        store.getRealm().executeTransactionAsync(transaction -> store.getRealm().insert(realmObject));
    }

    public void execute(Realm.Transaction transaction) {
        $(StoreHandler.class).getStore().getRealm().executeTransactionAsync(transaction);
    }

    private String newLocalId() {
        return PhoneUtil.rndId();
    }
}
