package closer.vlllage.com.closer.handler.quest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.databinding.QuestLinkItemBinding
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LightDarkHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.Quest
import com.queatz.on.On


class QuestLinkAdapter(on: On, private val onQuestLongClickListener: ((Quest) -> Unit)? = null, private val onQuestClickListener: ((Quest, View) -> Unit)? = null) : PoolRecyclerAdapter<QuestLinkAdapter.QuestLinkViewHolder>(on) {

    private val quests = mutableListOf<Quest>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = QuestLinkViewHolder(QuestLinkItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: QuestLinkViewHolder, position: Int) {
        val quest = quests[position]

        holder.on = On(on).apply {
            use<DisposableHandler>()
        }

        holder.on<LightDarkHandler>().onLightChanged.subscribe {
            holder.binding.name.compoundDrawableTintList = it.tint
            holder.binding.name.setTextColor(it.text)
            holder.binding.name.setBackgroundResource(it.clickableRoundedBackground)
        }.also {
            holder.on<DisposableHandler>().add(it)
        }

        holder.binding.name.text = quest.name ?: ""

        holder.binding.name.setOnClickListener {
            onQuestClickListener?.invoke(quest, holder.binding.name)
        }
        holder.binding.name.setOnLongClickListener {
            onQuestLongClickListener?.invoke(quest)
            true
        }
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

    inner class QuestLinkViewHolder(val binding: QuestLinkItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var on: On
    }
}
