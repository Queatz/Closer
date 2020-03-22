package closer.vlllage.com.closer.handler.share

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.SearchGroupsAdapter
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.ui.RecyclerViewHeader
import com.queatz.on.On

class SearchGroupsHeaderAdapter constructor(on: On,
                                onGroupClickListener: ((Group, View) -> Unit)?,
                                onCreateGroupClickListener: ((String) -> Unit)?,
                                private val onQueryChangedListener: OnQueryChangedListener)
    : SearchGroupsAdapter(on, false, onGroupClickListener, onCreateGroupClickListener) {

    private val header = RecyclerViewHeader()
    private var headerText: String? = null

    init {
        setNoAnimation(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            HEADER_VIEW_TYPE -> return SearchGroupsHeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_text_header_item, parent, false))
        }

        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            when (holder) {
                is SearchGroupsHeaderViewHolder -> {
                    holder.name.text = headerText
                    holder.searchGroups.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                        }

                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                            onQueryChangedListener.onQueryChanged(s.toString())
                        }

                        override fun afterTextChanged(s: Editable) {

                        }
                    })
                }
            }
        } else {
            super.onBindViewHolder(holder, position - 1)
        }

        header.onBind(holder, position)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
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

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    fun setHeaderText(headerText: String): SearchGroupsHeaderAdapter {
        this.headerText = headerText
        return this
    }

    interface OnQueryChangedListener {
        fun onQueryChanged(query: String)
    }

    companion object {
        private const val HEADER_VIEW_TYPE = -1
    }

    class SearchGroupsHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val searchGroups: EditText = view.findViewById(R.id.searchGroups)
    }

}
