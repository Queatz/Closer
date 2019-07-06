package closer.vlllage.com.closer.handler.group

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import closer.vlllage.com.closer.R

class GroupActionBarButton @JvmOverloads constructor(
        var name: String?,
        var onClick: View.OnClickListener?,
        var onLongClick: View.OnClickListener? = null,
        @param:DrawableRes @field:DrawableRes var backgroundDrawableRes: Int = R.drawable.clickable_accent,
        @param:ColorRes @field:ColorRes var textColorRes: Int = R.color.text) {
    @DrawableRes var icon: Int = 0
}
