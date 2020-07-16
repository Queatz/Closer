package closer.vlllage.com.closer.handler.quest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LightDarkHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.Quest
import com.queatz.on.On
import kotlinx.android.synthetic.main.quest_link_item.view.*


class QuestLinkAdapter(on: On, onQuestClickListener: ((Quest) -> Unit)? = null) : PoolRecyclerAdapter<QuestLinkAdapter.QuestLinkViewHolder>(on) {

    private val quests = mutableListOf<Quest>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = QuestLinkViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.quest_link_item, parent, false))

    override fun onBindViewHolder(holder: QuestLinkViewHolder, position: Int) {
        val quest = quests[position]

        holder.on = On(on).apply {
            use<DisposableHandler>()
        }

        holder.on<LightDarkHandler>().onLightChanged.subscribe {
            holder.itemView.name.compoundDrawableTintList = it.tint
            holder.itemView.name.setTextColor(it.text)
            holder.itemView.name.setBackgroundResource(it.clickableRoundedBackground)
        }.also {
            holder.on<DisposableHandler>().add(it)
        }

        holder.itemView.name.text = quest.name ?: ""
    }

    override fun getItemCount() = quests.size

    fun setQuests(groupActions: List<Quest>, disableAnimation: Boolean = false) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = this@QuestLinkAdapter.quests.size
            override fun getNewListSize() = groupActions.size

            override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@QuestLinkAdapter.quests[oldPosition].id == groupActions[newPosition].id
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@QuestLinkAdapter.quests[oldPosition].name == groupActions[newPosition].name
            }
        }, true)
        this.quests.clear()
        this.quests.addAll(groupActions)

        if (disableAnimation) notifyDataSetChanged()
        else diffResult.dispatchUpdatesTo(this)
    }

    inner class QuestLinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var on: On
    }
}
