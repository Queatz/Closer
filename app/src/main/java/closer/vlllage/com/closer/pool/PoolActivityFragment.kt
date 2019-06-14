package closer.vlllage.com.closer.pool

import androidx.fragment.app.Fragment
import com.queatz.on.On

open class PoolActivityFragment : Fragment() {

    protected val on: On
        get() = (activity as PoolActivity).on

}