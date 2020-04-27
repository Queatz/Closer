package closer.vlllage.com.closer.handler.post


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.ui.RecyclerViewHeader
import com.queatz.on.On
import kotlinx.android.synthetic.main.text_header_item.view.*

class CreatePostHeaderAdapter(on: On, action: (CreatePostAction) -> Unit) : CreatePostAdapter(on, action) {

    private val header = RecyclerViewHeader(on)
    private var headerViewHolder: HeaderViewHolder? = null
    private var headerText: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                HEADER_VIEW_TYPE -> HeaderViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.text_header_item, parent, false))
                else -> super.onCreateViewHolder(parent, viewType)
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            when (holder) {
                is HeaderViewHolder -> {
                    headerViewHolder = holder
                    holder.name.text = headerText
                }
            }
        } else {
            super.onBindViewHolder(holder, position - 1)
        }

        header.onBind(holder, position)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder === headerViewHolder) { headerViewHolder = null }

        super.onViewRecycled(holder)
        header.onRecycled(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        header.attach(recyclerView, on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.feedPeekHeight) * 2)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) HEADER_VIEW_TYPE else super.getItemViewType(position - 1)
    }

    override fun getItemCount() = super.getItemCount() + 1

    fun setHeaderText(text: String) {
        headerText = text
        headerViewHolder?.name?.text = text
    }

    companion object {
        private const val HEADER_VIEW_TYPE = -1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.name!!
    }
}
