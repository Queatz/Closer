package closer.vlllage.com.closer.store

import android.app.Application

import closer.vlllage.com.closer.store.models.MyObjectBox
import io.objectbox.Box
import io.objectbox.BoxStore

class Store internal constructor(app: Application) {

    private val boxStore: BoxStore

    init {
        boxStore = MyObjectBox.builder().androidContext(app).build()
    }

    fun close() {
        boxStore.close()
    }

    fun <T> box(clazz: Class<T>): Box<T> {
        return boxStore.boxFor(clazz)
    }
}
