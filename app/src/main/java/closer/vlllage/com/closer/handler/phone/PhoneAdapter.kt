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
import closer.vlllage.com.closer.handler.helpers.Val
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import com.queatz.on.On
import java.util.*

open class PhoneAdapter(on: On, private val onReactionClickListener: (ReactionResult) -> Unit) : PoolRecyclerAdapter<PhoneAdapter.ViewHolder>(on) {
    var items: List<ReactionResult> = ArrayList()
        set(items) {
            field = items
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.phone_item, viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val reaction = this.items[position]

        viewHolder.name.text = on<NameHandler>().getName(reaction.phone!!.id!!)
        viewHolder.reaction.text = reaction.reaction

        if (on<Val>().isEmpty(reaction.phone?.photo)) {
            viewHolder.photo.visibility = View.GONE
        } else {
            viewHolder.photo.visibility = View.VISIBLE
            on<PhotoHelper>().loadCircle(viewHolder.photo, reaction.phone!!.photo!!)
            viewHolder.photo.setOnClickListener { v -> on<PhotoActivityTransitionHandler>().show(viewHolder.photo, reaction.phone!!.photo!!) }
        }

        viewHolder.itemView.setOnClickListener { v -> onReactionClickListener.invoke(reaction) }
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var photo: ImageView = itemView.findViewById(R.id.photo)
        var name: TextView = itemView.findViewById(R.id.name)
        var reaction: TextView = itemView.findViewById(R.id.reaction)
    }
}
