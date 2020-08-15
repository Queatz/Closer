package closer.vlllage.com.closer.store

import android.app.Application
import closer.vlllage.com.closer.store.models.MyObjectBox
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.TxCallback
import kotlin.reflect.KClass

class Store internal constructor(app: Application) {

    private val boxStore: BoxStore = MyObjectBox.builder().androidContext(app).build()

    fun tx(runnable: () -> Unit, callback: TxCallback<Void>? = null) = boxStore.runInTxAsync({ runnable.invoke() }, callback)
    fun close() = boxStore.close()
    fun <T : Any> box(clazz: Class<T>): Box<T> = boxStore.boxFor(clazz)
    fun <T : Any> box(clazz: KClass<T>): Box<T> = boxStore.boxFor(clazz.java)
}
