package closer.vlllage.com.closer.handler.group

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.Phone
import java.util.*

class MentionAdapter(poolMember: PoolMember, private val onMentionClickListener: ((Phone) -> Unit)?) : PoolRecyclerAdapter<MentionAdapter.ViewHolder>(poolMember) {

    private val items = ArrayList<Phone>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.group_action_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val phone = items[position]
        holder.mentionName.text = items[position].name

        holder.mentionName.setOnClickListener {
            onMentionClickListener?.invoke(phone)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(items: List<Phone>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return this@MentionAdapter.items.size
            }

            override fun getNewListSize(): Int {
                return items.size
            }

            override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@MentionAdapter.items[oldPosition].id == items[newPosition].id
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@MentionAdapter.items[oldPosition].name == items[newPosition].name
            }
        }, true)
        this.items.clear()
        this.items.addAll(items)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var mentionName: TextView

        init {
            itemView.clipToOutline = true
            mentionName = itemView.findViewById(R.id.actionName)
        }
    }
}
