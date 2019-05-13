package closer.vlllage.com.closer.handler.phone

import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.ReactionResult
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.ui.RecyclerViewHeader

class PhoneAdapterHeaderAdapter(poolMember: PoolMember, onReactionClickListener: (ReactionResult) -> Unit) : PhoneAdapter(poolMember, onReactionClickListener) {

    private val header = RecyclerViewHeader()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        header.onBind(holder, position)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        header.onRecycled(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        header.attach(recyclerView, `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.feedPeekHeight) * 2)
    }
}
