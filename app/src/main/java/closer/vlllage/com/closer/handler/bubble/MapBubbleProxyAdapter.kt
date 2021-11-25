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
import closer.vlllage.com.closer.handler.group.PhysicalGroupHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.*
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.queatz.on.On
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

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
        holder.on = On(on).apply {
            use<DisposableHandler>()
        }

        when (mapBubble.type) {
            BubbleType.STATUS -> {
                holder.name.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textInverse))
                holder.info.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse))
            }
            else -> {
                holder.name.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.text))
                holder.info.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textHint))
            }
        }

        holder.background.visible = false
        holder.background.clipToOutline = true

        when (mapBubble.type) {
            BubbleType.STATUS -> {
                holder.click.setBackgroundResource(R.drawable.clickable_white_8dp)
                holder.photo.visible = true
                holder.photo.colorFilter = null
                holder.photo.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(android.R.color.transparent))
                holder.photo.setImageResource(R.drawable.ic_person_black_24dp)
                holder.name.text = "${mapBubble.name}${on<PhoneDetailsHandler>().detailsOf(mapBubble.tag as Phone)}\n${mapBubble.status ?: ""}"
                holder.info.visible = true
                holder.info.text = on<TimeStr>().pretty((mapBubble.tag as Phone).updated)

                if (mapBubble.tag is Phone) {
                    val phone = mapBubble.tag as Phone
                    if (!phone.photo.isNullOrBlank()) {
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
                holder.info.visible = true
                holder.info.text = on<TimeStr>().prettyDate((mapBubble.tag as Suggestion).created)
            }
            BubbleType.PHYSICAL_GROUP -> {
                holder.click.setBackgroundResource(R.drawable.clickable_purple_8dp)
                holder.photo.visible = true
                holder.info.visible = false

                if ((mapBubble.tag != null) and (mapBubble.tag is Group)) {
                    val group = mapBubble.tag as Group

                    if (group.photo != null) {
                        holder.background.setImageDrawable(null)
                        holder.background.visible = true
                        on<ImageHandler>().get()
                                .load(group.photo!! + "?s=128")
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(holder.background)
                    }

                    holder.photo.setImageResource(R.drawable.ic_chat_black_24dp)
                    holder.photo.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(android.R.color.white))
                }

                on<PhysicalGroupHandler>().physicalGroupName((mapBubble.tag as Group))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                    holder.name.text = it
                }, {}).also {
                    holder.on<DisposableHandler>().add(it)
                }
            }
            BubbleType.EVENT -> {
                holder.click.setBackgroundResource(R.drawable.clickable_red_8dp)
                holder.photo.visible = false
                holder.info.visible = false

                val event = mapBubble.tag as Event

                holder.name.text = "${event.name}\n${on<EventDetailsHandler>().formatEventDetails(event)}"

                if (event.isPublic) {
                    holder.name.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                } else {
                    holder.name.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_group_black_18dp, 0, 0, 0)
                }
            }
            else -> {
                holder.name.visible = true
                holder.name.text = on<ResourcesHandler>().resources.getString(R.string.unknown)
            }
        }

        holder.itemView.setOnClickListener { view ->
            mapBubble.view = view
            onClickListener.invoke(mapBubble)
        }
    }

    override fun onViewRecycled(holder: ProxyMapBubbleViewHolder) {
        holder.on.off()
    }

    override fun getItemCount() = items.size

    inner class ProxyMapBubbleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var click: View = itemView.findViewById(R.id.click)
        var photo: ImageView = itemView.findViewById(R.id.photo)
        var background: ImageView = itemView.findViewById(R.id.background)
        var name: TextView = itemView.findViewById(R.id.name)
        var info: TextView = itemView.findViewById(R.id.info)
        lateinit var on: On
    }
}
