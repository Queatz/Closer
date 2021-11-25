package closer.vlllage.com.closer.pool

import android.os.Bundle
import androidx.fragment.app.Fragment
import closer.vlllage.com.closer.App
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.CameraHandler
import closer.vlllage.com.closer.handler.media.MediaHandler
import com.queatz.on.On

open class PoolFragment : Fragment() {

    protected val on = On()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        on<ActivityHandler>().activity = activity

        val activityOn = (activity as PoolActivity).on
        on.use(activityOn<MediaHandler>())
        on.use(activityOn<CameraHandler>())

        on<ApplicationHandler>().app = requireActivity().application as App
        on<ApiHandler>().setAuthorization(on<AccountHandler>().phone)
    }

    override fun onDestroy() {
        on.off()
        super.onDestroy()
    }
}
