package closer.vlllage.com.closer.pool

import androidx.multidex.MultiDexApplication
import com.queatz.on.On

abstract class PoolApplication : MultiDexApplication() {

    internal val on = On()

    override fun onTerminate() {
        on.off()
        super.onTerminate()
    }
}
