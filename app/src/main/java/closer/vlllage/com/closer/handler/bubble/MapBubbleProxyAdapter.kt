package closer.vlllage.com.closer.handler.bubble

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.group.GroupMessageParseHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class MapBubbleProxyAdapter(on: On, private val onClickListener: (MapBubble) -> Unit)
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
                holder.click.setBackgroundResource(R.drawable.clickable_white_8dp)
                holder.photo.visible = true
                holder.photo.colorFilter = null
                holder.photo.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(android.R.color.transparent))
                holder.photo.setImageResource(R.drawable.ic_person_black_24dp)
                holder.name.text = mapBubble.name + (on<PhoneDetailsHandler>().detailsOf(mapBubble.tag as Phone, true)) + "\n" + mapBubble.status
                holder.info.visible = false

                if (mapBubble.tag is Phone) {
                    val phone = mapBubble.tag as Phone
                    if (!on<Val>().isEmpty(phone.photo)) {
                        on<PhotoHelper>().loadCircle(holder.photo, phone.photo!!)
                    }
                }
            }
            BubbleType.SUGGESTION -> {
                holder.click.setBackgroundResource(R.drawable.clickable_blue_light_8dp)
                holder.photo.visible = true
                holder.photo.colorFilter = null
                holder.photo.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(android.R.color.white))
                holder.photo.setImageResource(R.drawable.ic_edit_location_black_24dp)
                holder.name.text = mapBubble.status
            }
            BubbleType.PHYSICAL_GROUP -> {
                holder.click.setBackgroundResource(R.drawable.clickable_purple_8dp)
                holder.photo.visible = true
                holder.info.visible = false

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
                        holder.photo.setImageResource(R.drawable.ic_chat_black_24dp)
                        holder.photo.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(android.R.color.white))
                    }
                }

                holder.name.text = on<Val>().of((mapBubble.tag as Group).name, on<StoreHandler>().store.box(GroupMessage::class).query()
                        .equal(GroupMessage_.to, (mapBubble.tag as Group).id ?: "")
                        .order(GroupMessage_.updated)
                        .build().findFirst()?.text?.let { on<GroupMessageParseHandler>().parseString(it) }?.let { "\"${it}\"" } ?: on<ResourcesHandler>().resources.getString(R.string.app_name))
            }
            BubbleType.EVENT -> {
                holder.click.setBackgroundResource(R.drawable.clickable_red_8dp)
                holder.photo.visible = false
                holder.info.visible = false

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
