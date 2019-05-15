package closer.vlllage.com.closer.pool

import androidx.recyclerview.widget.RecyclerView
import com.queatz.on.On

abstract class PoolRecyclerAdapter<T : RecyclerView.ViewHolder> constructor(protected val on: On) : RecyclerView.Adapter<T>()