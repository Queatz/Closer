package closer.vlllage.com.closer.handler.bubble

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ImageHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.models.Group
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class MapBubblePhysicalGroupView : PoolMember() {
    fun from(layer: ViewGroup, mapBubble: MapBubble, onClickListener: MapBubblePhysicalGroupClickListener): View {
        val view = LayoutInflater.from(layer.context).inflate(R.layout.map_bubble_physical_group, layer, false)

        view.findViewById<View>(R.id.click).setOnClickListener { v -> onClickListener.invoke(mapBubble) }
        update(view, mapBubble)

        return view
    }

    fun update(view: View, mapBubble: MapBubble) {
        var margin = `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.pad)
        var size = `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.physicalGroupIcon)
        val photo = view.findViewById<ImageView>(R.id.photo)
        if ((mapBubble.tag != null) and (mapBubble.tag is Group)) {
            val group = mapBubble.tag as Group
            if (group.photo != null) {
                margin /= 4
                size = `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.physicalGroupPhoto)
                photo.colorFilter = null
                photo.imageTintList = ColorStateList.valueOf(`$`(ResourcesHandler::class.java).resources.getColor(android.R.color.transparent))
                `$`(ImageHandler::class.java).get()
                        .load(group.photo!! + "?s=32")
                        .fit()
                        .transform(RoundedCornersTransformation(`$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.physicalGroupCorners), 0))
                        .into(photo)
            } else {
                photo.setImageResource(R.drawable.ic_wifi_black_24dp)
                photo.imageTintList = ColorStateList.valueOf(`$`(ResourcesHandler::class.java).resources.getColor(android.R.color.white))
            }
        }

        (photo.layoutParams as ViewGroup.MarginLayoutParams).setMargins(margin, margin, margin, margin)
        (photo.layoutParams as ViewGroup.MarginLayoutParams).height = size
        (photo.layoutParams as ViewGroup.MarginLayoutParams).width = size
    }

}

typealias MapBubblePhysicalGroupClickListener = (mapBubble: MapBubble) -> Unit
