package closer.vlllage.com.closer.pool

import android.os.Bundle
import androidx.fragment.app.Fragment
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import com.queatz.on.On

open class PoolActivityFragment : Fragment() {

    protected lateinit var on: On


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        on = On((activity as PoolActivity).on).apply {
            use<DisposableHandler>()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        on.off()
    }
}