package closer.vlllage.com.closer.handler.bubble

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.group.PhotoActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.PhoneDetailsHandler
import closer.vlllage.com.closer.handler.helpers.PhotoHelper
import closer.vlllage.com.closer.handler.helpers.TimeStr
import closer.vlllage.com.closer.handler.helpers.Val
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On

/**
 * Created by jacob on 2/18/18.
 */

class MapBubbleView constructor(private val on: On) {
    fun from(layer: ViewGroup, mapBubble: MapBubble, onClickListener: (MapBubble) -> Unit): View {
        val view = LayoutInflater.from(layer.context).inflate(R.layout.map_bubble, layer, false)

        view.findViewById<View>(R.id.click).setOnClickListener { v -> onClickListener.invoke(mapBubble) }
        update(view, mapBubble)

        return view
    }

    fun update(view: View, mapBubble: MapBubble) {

        val name = view.findViewById<TextView>(R.id.name)
        val info = view.findViewById<TextView>(R.id.info)
        val action = view.findViewById<TextView>(R.id.action)
        val photo = view.findViewById<ImageView>(R.id.photo)

        if (mapBubble.inProxy) {
            name.visible = true
            name.text = mapBubble.name + on<PhoneDetailsHandler>().detailsOf(mapBubble.tag as Phone)

            info?.text = on<TimeStr>().pretty((mapBubble.tag as Phone).updated)
        } else {
            if (mapBubble.name!!.isEmpty()) {
                name.visible = false
            } else {
                name.visible = true
                name.text = mapBubble.name + on<PhoneDetailsHandler>().detailsOf(mapBubble.tag as Phone)
            }

            (view.findViewById<View>(R.id.status) as? TextView)?.text = on<Val>().trimmed(mapBubble.status)

            if (mapBubble.action != null && action != null) {
                action.text = mapBubble.action
            } else if (mapBubble.tag is Phone) {
                val phone = mapBubble.tag as Phone

                action?.text = on<TimeStr>().pretty(phone.updated)

                if (photo != null) {
                    if (!phone.photo.isNullOrBlank()) {
                        photo.imageTintList = null
                        on<PhotoHelper>().loadCircle(photo, phone.photo!!)
                        photo.setOnClickListener { v -> on<PhotoActivityTransitionHandler>().show(photo, phone.photo!!) }
                    } else {
                        photo.imageTintList = ColorStateList.valueOf(on<PhotoHelper>().colorForPhone(phone.id!!))
                    }
                }
            }
        }
    }
}
