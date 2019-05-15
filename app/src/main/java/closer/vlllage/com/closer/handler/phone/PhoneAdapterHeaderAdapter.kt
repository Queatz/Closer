package closer.vlllage.com.closer.handler.phone

import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.ReactionResult
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.ui.RecyclerViewHeader
import com.queatz.on.On

class PhoneAdapterHeaderAdapter(on: On, onReactionClickListener: (ReactionResult) -> Unit) : PhoneAdapter(on, onReactionClickListener) {

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
        header.attach(recyclerView, on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.feedPeekHeight) * 2)
    }
}
