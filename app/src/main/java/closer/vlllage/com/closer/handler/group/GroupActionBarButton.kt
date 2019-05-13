package closer.vlllage.com.closer.handler.group

import android.support.annotation.DrawableRes
import android.view.View

import closer.vlllage.com.closer.R

class GroupActionBarButton @JvmOverloads constructor(var name: String?, var onClick: View.OnClickListener?, var onLongClick: View.OnClickListener? = null, @param:DrawableRes @field:DrawableRes var backgroundDrawableRes: Int = R.drawable.clickable_accent) {
    @DrawableRes
    var icon: Int = 0

}
