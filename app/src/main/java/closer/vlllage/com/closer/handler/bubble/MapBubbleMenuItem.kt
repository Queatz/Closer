package closer.vlllage.com.closer.handler.bubble

import android.support.annotation.DrawableRes

data class MapBubbleMenuItem constructor(
    var title: String,
    @DrawableRes var iconRes: Int? = null
)
