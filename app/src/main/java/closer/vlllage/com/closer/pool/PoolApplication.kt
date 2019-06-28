package closer.vlllage.com.closer.pool

import android.app.Application
import com.queatz.on.On

abstract class PoolApplication : Application() {

    internal val on = On()

    override fun onTerminate() {
        on.off()
        super.onTerminate()
    }
}
