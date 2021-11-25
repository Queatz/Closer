package closer.vlllage.com.closer.handler.phone

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.ReactionResult
import closer.vlllage.com.closer.databinding.ListTextHeaderItemBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.ui.RecyclerViewHeader
import com.queatz.on.On

class PhoneAdapterHeaderAdapter(on: On, onReactionClickListener: (ReactionResult) -> Unit) : PhoneAdapter(on, onReactionClickListener) {

    private val header = RecyclerViewHeader(on)
    private var headerViewHolder: PhoneHeaderViewHolder? = null
    private var headerText: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            HEADER_VIEW_TYPE -> return PhoneHeaderViewHolder(ListTextHeaderItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
        }

        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            when (holder) {
                is PhoneHeaderViewHolder -> {
                    headerViewHolder = holder
                    holder.binding.name.text = headerText
                    holder.binding.loadingText.visible = isLoading || items.isEmpty()
                    holder.binding.loadingText.setText(if (isLoading) R.string.loading_people else R.string.nobody)
                    holder.binding.searchGroups.visible = false
                }
            }
        } else {
            super.onBindViewHolder(holder, position - 1)
        }

        header.onBind(holder, position)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder === headerViewHolder) {
            headerViewHolder = null
        }

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
        headerViewHolder?.binding?.name?.text = text
    }

    companion object {
        private const val HEADER_VIEW_TYPE = -1
    }
}

class PhoneHeaderViewHolder(val binding: ListTextHeaderItemBinding) : RecyclerView.ViewHolder(binding.root)
