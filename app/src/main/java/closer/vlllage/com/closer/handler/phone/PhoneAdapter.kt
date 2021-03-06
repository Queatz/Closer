package closer.vlllage.com.closer.handler.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.ReactionResult
import closer.vlllage.com.closer.handler.group.PhotoActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.PhotoHelper
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import com.queatz.on.On

open class PhoneAdapter(on: On, private val onReactionClickListener: (ReactionResult) -> Unit) : PoolRecyclerAdapter<RecyclerView.ViewHolder>(on) {
    var items: List<ReactionResult> = listOf()
        set(items) {
            field = items
            isLoading = false
            notifyDataSetChanged()
        }

    var isLoading = true

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.phone_item, viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is ViewHolder -> {
                val reaction = this.items[position]

                viewHolder.name.text = on<NameHandler>().getName(reaction.phone!!.id!!)
                viewHolder.reaction.text = reaction.reaction

                if (reaction.phone!!.photo == null) {
                    viewHolder.photo.setImageResource(R.drawable.ic_person_black_24dp)
                    viewHolder.photo.scaleType = ImageView.ScaleType.CENTER_INSIDE
                } else {
                    on<PhotoHelper>().loadCircle(viewHolder.photo, reaction.phone!!.photo!!)
                    viewHolder.photo.scaleType = ImageView.ScaleType.CENTER_CROP
                }
                viewHolder.photo.setOnClickListener { v -> on<PhotoActivityTransitionHandler>().show(viewHolder.photo, reaction.phone!!.photo!!) }

                viewHolder.itemView.setOnClickListener { v -> onReactionClickListener.invoke(reaction) }
            }
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var photo: ImageView = itemView.findViewById(R.id.photo)
        var name: TextView = itemView.findViewById(R.id.name)
        var reaction: TextView = itemView.findViewById(R.id.reaction)
    }
}
