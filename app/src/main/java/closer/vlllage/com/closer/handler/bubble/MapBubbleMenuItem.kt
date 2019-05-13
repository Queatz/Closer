package closer.vlllage.com.closer.handler.bubble

import androidx.annotation.DrawableRes

data class MapBubbleMenuItem constructor(
    var title: String,
    @DrawableRes var iconRes: Int? = null
)
