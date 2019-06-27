package closer.vlllage.com.closer.handler.phone

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.queatz.on.On
import kotlinx.android.synthetic.main.item_goal.view.*

class GoalAdapter constructor(private val on: On, private val callback: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var name: String = on<ResourcesHandler>().resources.getString(R.string.them)
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    lateinit var type: String

    var isRemove: Boolean = false

    var items = mutableListOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_goal, parent, false)) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        holder.itemView.setOnClickListener {
            callback.invoke(item)
        }

        holder.itemView.type.text = type
        holder.itemView.goalName.text = item
        holder.itemView.cheerButton.text = if (isRemove)
            on<ResourcesHandler>().resources.getString(R.string.remove)
        else
            on<ResourcesHandler>().resources.getString(R.string.cheer_them, name)
    }
}
