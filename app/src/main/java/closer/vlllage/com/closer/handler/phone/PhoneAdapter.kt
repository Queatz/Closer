package closer.vlllage.com.closer.handler.phone

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.ReactionResult
import closer.vlllage.com.closer.handler.group.PhotoActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.PhotoHelper
import closer.vlllage.com.closer.handler.helpers.Val
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import java.util.*

open class PhoneAdapter(poolMember: PoolMember, private val onReactionClickListener: (ReactionResult) -> Unit) : PoolRecyclerAdapter<PhoneAdapter.ViewHolder>(poolMember) {
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

        viewHolder.name.text = reaction.phone!!.name
        viewHolder.reaction.text = reaction.reaction

        if (`$`(Val::class.java).isEmpty(reaction.phone?.photo)) {
            viewHolder.photo.visibility = View.GONE
        } else {
            viewHolder.photo.visibility = View.VISIBLE
            `$`(PhotoHelper::class.java).loadCircle(viewHolder.photo, reaction.phone!!.photo!!)
            viewHolder.photo.setOnClickListener { v -> `$`(PhotoActivityTransitionHandler::class.java).show(viewHolder.photo, reaction.phone!!.photo!!) }
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
