package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DisposableGroup
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LightDarkHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On
import java.util.*

class MentionAdapter(on: On, private val onMentionClickListener: ((Phone) -> Unit)?) : PoolRecyclerAdapter<MentionAdapter.ViewHolder>(on) {

    private val items = mutableListOf<Phone>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.group_action_item, parent, false))

        holder.disposableGroup = on<DisposableHandler>().group()

        holder.disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            holder.mentionName.setBackgroundResource(it.clickableRoundedBackground)
            holder.mentionName.setTextColor(it.text)
        })

        return holder
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
        lateinit var disposableGroup: DisposableGroup

        init {
            itemView.clipToOutline = true
            mentionName = itemView.findViewById(R.id.actionName)
        }
    }
}
