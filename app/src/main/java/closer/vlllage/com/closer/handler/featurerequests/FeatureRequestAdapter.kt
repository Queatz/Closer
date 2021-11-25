package closer.vlllage.com.closer.handler.featurerequests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.FeatureRequestResult
import closer.vlllage.com.closer.databinding.ItemFeatureRequestBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.MenuHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import com.queatz.on.On

class FeatureRequestAdapter constructor(private val on: On) : RecyclerView.Adapter<FeatureRequestViewHolder>() {

    var items = mutableListOf<FeatureRequestResult>()
        set(value) {
            val diffUtil = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = field[oldItemPosition].id == value[newItemPosition].id

                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                        field[oldItemPosition].voted == value[newItemPosition].voted &&
                        field[oldItemPosition].votes == value[newItemPosition].votes &&
                        field[oldItemPosition].completed == value[newItemPosition].completed
            })

            field.clear()
            field.addAll(value)

            diffUtil.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            FeatureRequestViewHolder(ItemFeatureRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: FeatureRequestViewHolder, position: Int) {
        val item = items[position]

        holder.binding.name.text = item.name
        holder.binding.description.text = item.description
        holder.binding.voteCount.text = on<ResourcesHandler>().resources.getQuantityString(R.plurals.votes, item.votes, item.votes)

        holder.binding.card.setOnLongClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_check_black_24dp, if (item.completed) R.string.unmark_as_completed else R.string.mark_as_completed) {
                        on<FeatureRequestsHandler>().complete(item.id!!, !item.completed)
                    }
            )
            return@setOnLongClickListener false
        }

        holder.binding.badge.visible = item.completed || item.created?.after(on<TimeAgo>().daysAgo(3)) ?: false

        if (item.completed) {
            holder.binding.badge.setText(R.string.completed_request)
            holder.binding.badge.setBackgroundResource(R.drawable.clickable_green_light)
        } else {
            holder.binding.badge.setBackgroundResource(R.drawable.clickable_red_light)
            holder.binding.badge.setText(R.string.new_request)
        }

        holder.binding.voteButton.text = on<ResourcesHandler>().resources.getString(
                if (item.voted) R.string.you_voted else R.string.vote
        )
        holder.binding.voteButton.setOnClickListener {
            on<FeatureRequestsHandler>().vote(item.id!!, !item.voted)
        }
    }
}

class FeatureRequestViewHolder(val binding: ItemFeatureRequestBinding) : RecyclerView.ViewHolder(binding.root)

