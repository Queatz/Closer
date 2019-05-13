package closer.vlllage.com.closer.handler.bubble

import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Phone
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.util.*

class MapBubbleProxyAdapter(poolMember: PoolMember, private val proxyMapBubble: MapBubble, private val onClickListener: (MapBubble) -> Unit) : PoolRecyclerAdapter<MapBubbleProxyAdapter.ProxyMapBubbleViewHolder>(poolMember) {
    private val items = ArrayList<MapBubble>()

    fun setItems(items: List<MapBubble>): MapBubbleProxyAdapter {
        this.items.clear()
        this.items.addAll(items)
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProxyMapBubbleViewHolder {
        return ProxyMapBubbleViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.map_bubble_proxy_item, parent, false))
    }

    override fun onBindViewHolder(holder: ProxyMapBubbleViewHolder, position: Int) {
        val mapBubble = items[position]

        when (mapBubble.type) {
            BubbleType.STATUS -> {
                holder.name.setTextColor(`$`(ResourcesHandler::class.java).resources.getColor(R.color.textInverse))
                holder.info.setTextColor(`$`(ResourcesHandler::class.java).resources.getColor(R.color.textInverse))
            }
            else -> {
                holder.name.setTextColor(`$`(ResourcesHandler::class.java).resources.getColor(R.color.text))
                holder.info.setTextColor(`$`(ResourcesHandler::class.java).resources.getColor(R.color.text))
            }
        }

        when (mapBubble.type) {
            BubbleType.STATUS -> {
                holder.click.setBackgroundResource(R.drawable.clickable_white_4dp)
                holder.photo.visibility = View.VISIBLE
                holder.photo.colorFilter = null
                holder.photo.imageTintList = ColorStateList.valueOf(`$`(ResourcesHandler::class.java).resources.getColor(android.R.color.transparent))
                holder.photo.setImageResource(R.drawable.ic_person_black_24dp)
                holder.name.text = mapBubble.name + "\n" + mapBubble.status
                if (mapBubble.tag is Phone) {
                    holder.info.visibility = View.VISIBLE
                    val phone = mapBubble.tag as Phone
                    holder.info.text = `$`(TimeStr::class.java).pretty(phone.updated)
                    if (!`$`(Val::class.java).isEmpty(phone.photo)) {
                        `$`(PhotoHelper::class.java).loadCircle(holder.photo, phone.photo!!)
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
                        holder.photo.imageTintList = ColorStateList.valueOf(`$`(ResourcesHandler::class.java).resources.getColor(android.R.color.transparent))
                        `$`(ImageHandler::class.java).get()
                                .load(group.photo!! + "?s=32")
                                .fit()
                                .transform(RoundedCornersTransformation(`$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.physicalGroupCorners), 0))
                                .into(holder.photo)
                    } else {
                        holder.photo.setImageResource(R.drawable.ic_wifi_black_24dp)
                        holder.photo.imageTintList = ColorStateList.valueOf(`$`(ResourcesHandler::class.java).resources.getColor(android.R.color.white))
                    }
                }

                holder.name.text = `$`(Val::class.java).of((mapBubble.tag as Group).name!!, `$`(ResourcesHandler::class.java).resources.getString(R.string.app_name))
            }
            BubbleType.EVENT -> {
                holder.click.setBackgroundResource(R.drawable.clickable_red_4dp)
                holder.photo.visibility = View.GONE
                holder.info.visibility = View.GONE

                val event = mapBubble.tag as Event

                holder.name.text = event.name + "\n" + `$`(EventDetailsHandler::class.java).formatEventDetails(event)

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

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ProxyMapBubbleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var click: View
        var photo: ImageView
        var name: TextView
        var info: TextView

        init {
            click = itemView.findViewById(R.id.click)
            photo = itemView.findViewById(R.id.photo)
            name = itemView.findViewById(R.id.name)
            info = itemView.findViewById(R.id.info)
        }
    }
}
