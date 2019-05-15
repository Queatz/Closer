package closer.vlllage.com.closer.store

import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.Val
import closer.vlllage.com.closer.store.models.BaseObject
import com.queatz.on.On
import com.queatz.on.OnLifecycle
import io.objectbox.Property
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.SubscriptionBuilder
import java.lang.reflect.InvocationTargetException
import java.util.*

class StoreHandler constructor(private val on: On) : OnLifecycle {

    lateinit var store: Store
        private set

    override fun on() {
        store = on<ApplicationHandler>().app.on<StoreRefHandler>().get()
    }

    fun <T : BaseObject> create(clazz: Class<T>): T? {
        try {
            val baseObject = clazz.getConstructor().newInstance()
            baseObject.id = on<Val>().rndId()
            store.box(clazz).put(baseObject)
            return baseObject
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        return null
    }

    fun <T : BaseObject> removeAllExcept(clazz: Class<T>, idProperty: Property<T>, idsToKeep: Collection<String>) {
        val query = store.box(clazz).query()

        var isNotFirst = false
        for (idToKeep in idsToKeep) {
            if (isNotFirst) {
                query.and()
            } else {
                isNotFirst = true
            }

            query.notEqual(idProperty, idToKeep)
        }

        query.build().subscribe().single().on(AndroidScheduler.mainThread()).observer { toDelete -> store.box(clazz).remove(toDelete) }
    }

    fun <T : BaseObject> findAll(clazz: Class<T>, idProperty: Property<T>, ids: Collection<String>): SubscriptionBuilder<List<T>> {
        return findAll(clazz, idProperty, ids, null)
    }

    fun <T : BaseObject> findAll(clazz: Class<T>, idProperty: Property<T>, ids: Collection<String>, sort: Comparator<T>?): SubscriptionBuilder<List<T>> {
        val query = store.box(clazz).query()

        var isNotFirst = false
        if (ids.isEmpty()) {
            query.`in`(idProperty, arrayOf<String>())
        } else
            for (id in ids) {
                if (isNotFirst) {
                    query.or()
                } else {
                    isNotFirst = true
                }

                query.equal(idProperty, id)
            }

        if (sort != null) {
            query.sort(sort)
        }

        return query.build().subscribe().single().on(AndroidScheduler.mainThread())
    }
}
