package closer.vlllage.com.closer.handler.map

import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.ui.RecyclerViewHeader

abstract class HeaderAdapter<T : RecyclerView.ViewHolder>(poolMember: PoolMember) : PoolRecyclerAdapter<T>(poolMember) {

    private val header = RecyclerViewHeader()

    override fun onBindViewHolder(holder: T, position: Int) {
        header.onBind(holder, position)
    }

    override fun onViewRecycled(holder: T) {
        header.onRecycled(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        header.attach(recyclerView, `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.feedPeekHeight))
    }
}
