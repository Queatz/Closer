package closer.vlllage.com.closer.pool

import android.os.Bundle
import androidx.fragment.app.Fragment
import closer.vlllage.com.closer.App
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On

open class PoolFragment : Fragment() {

    protected val on = On()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        on<ActivityHandler>().activity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        on<ApplicationHandler>().app = activity?.application as App
    }

    override fun onDestroy() {
        on.off()
        super.onDestroy()
    }
}
