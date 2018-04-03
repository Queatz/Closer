package closer.vlllage.com.closer.store;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import closer.vlllage.com.closer.handler.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.BaseObject;
import closer.vlllage.com.closer.util.PhoneUtil;
import io.objectbox.Property;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;
import io.objectbox.reactive.SubscriptionBuilder;

public class StoreHandler extends PoolMember {

    private Store store;

    @Override
    protected void onPoolInit() {
        store = $(ApplicationHandler.class).getApp().$(StoreRefHandler.class).get();
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

    public <T extends BaseObject> void removeAllExcept(Class<T> clazz, Property idProperty, Collection<String> idsToKeep) {
        QueryBuilder<T> query = store.box(clazz).query();

        boolean isNotFirst = false;
        for (String idToKeep : idsToKeep) {
            if (isNotFirst) {
                query.and();
            } else {
                isNotFirst = true;
            }

            query.notEqual(idProperty, idToKeep);
        }

        query.build().subscribe().single().on(AndroidScheduler.mainThread()).observer(
                toDelete -> store.box(clazz).remove(toDelete)
        );
    }

    public <T extends BaseObject> SubscriptionBuilder<List<T>> findAll(Class<T> clazz, Property idProperty, Collection<String> ids) {
        QueryBuilder<T> query = store.box(clazz).query();

        boolean isNotFirst = false;
        for (String id : ids) {
            if (isNotFirst) {
                query.or();
            } else {
                isNotFirst = true;
            }

            query.equal(idProperty, id);
        }

        return query.build().subscribe().single().on(AndroidScheduler.mainThread());
    }
}
