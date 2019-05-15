package closer.vlllage.com.closer.handler.bubble

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.helpers.*
import com.queatz.on.On
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Phone
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class MapBubbleProxyAdapter(on: On,
                            private val proxyMapBubble: MapBubble,
                            private val onClickListener: (MapBubble) -> Unit)
    : PoolRecyclerAdapter<MapBubbleProxyAdapter.ProxyMapBubbleViewHolder>(on) {

    var items = mutableListOf<MapBubble>()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProxyMapBubbleViewHolder {
        return ProxyMapBubbleViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.map_bubble_proxy_item, parent, false))
    }

    override fun onBindViewHolder(holder: ProxyMapBubbleViewHolder, position: Int) {
        val mapBubble = items[position]

        when (mapBubble.type) {
            BubbleType.STATUS -> {
                holder.name.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textInverse))
                holder.info.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textInverse))
            }
            else -> {
                holder.name.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.text))
                holder.info.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.text))
            }
        }

        when (mapBubble.type) {
            BubbleType.STATUS -> {
                holder.click.setBackgroundResource(R.drawable.clickable_white_4dp)
                holder.photo.visibility = View.VISIBLE
                holder.photo.colorFilter = null
                holder.photo.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(android.R.color.transparent))
                holder.photo.setImageResource(R.drawable.ic_person_black_24dp)
                holder.name.text = mapBubble.name + "\n" + mapBubble.status
                if (mapBubble.tag is Phone) {
                    holder.info.visibility = View.VISIBLE
                    val phone = mapBubble.tag as Phone
                    holder.info.text = on<TimeStr>().pretty(phone.updated)
                    if (!on<Val>().isEmpty(phone.photo)) {
                        on<PhotoHelper>().loadCircle(holder.photo, phone.photo!!)
                    }

                } else {
                    holder.info.visibility = View.GONE
                }
            }
            BubbleType.PHYSICAL_GROUP -> {
                holder.click.setBackgroundResource(R.drawable.clickable_purple_4dp)
                holder.photo.visibility = View.VISIBLE
                holder.info.visibility = View.GONE

                if ((mapBubble.tag != null) and (mapBubble.tag is Group)) {
                    val group = mapBubble.tag as Group
                    if (group.photo != null) {
                        holder.photo.colorFilter = null
                        holder.photo.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(android.R.color.transparent))
                        on<ImageHandler>().get()
                                .load(group.photo!! + "?s=32")
                                .fit()
                                .transform(RoundedCornersTransformation(on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.physicalGroupCorners), 0))
                                .into(holder.photo)
                    } else {
                        holder.photo.setImageResource(R.drawable.ic_wifi_black_24dp)
                        holder.photo.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(android.R.color.white))
                    }
                }

                holder.name.text = on<Val>().of((mapBubble.tag as Group).name, on<ResourcesHandler>().resources.getString(R.string.app_name))
            }
            BubbleType.EVENT -> {
                holder.click.setBackgroundResource(R.drawable.clickable_red_4dp)
                holder.photo.visibility = View.GONE
                holder.info.visibility = View.GONE

                val event = mapBubble.tag as Event

                holder.name.text = event.name + "\n" + on<EventDetailsHandler>().formatEventDetails(event)

                if (event.isPublic) {
                    holder.name.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                } else {
                    holder.name.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_black_18dp, 0, 0, 0)
                }
            }
        }

        holder.itemView.setOnClickListener { view ->
            mapBubble.view = view
            onClickListener.invoke(mapBubble)
        }
    }

    override fun getItemCount() = items.size

    inner class ProxyMapBubbleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var click: View = itemView.findViewById(R.id.click)
        var photo: ImageView = itemView.findViewById(R.id.photo)
        var name: TextView = itemView.findViewById(R.id.name)
        var info: TextView = itemView.findViewById(R.id.info)
    }
}
