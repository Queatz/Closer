package closer.vlllage.com.closer.handler.helpers

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On

class GroupColorHandler constructor(private val on: On) {

    @ColorInt
    fun getColor(group: Group): Int {
        return when {
            group.hasEvent() -> on<ResourcesHandler>().resources.getColor(R.color.white)
            group.hasPhone() -> on<ResourcesHandler>().resources.getColor(R.color.white)
            group.physical -> on<ResourcesHandler>().resources.getColor(R.color.purple)
            group.ofKind == "quest" -> on<ResourcesHandler>().resources.getColor(R.color.forestgreen)
            group.isPublic -> on<ResourcesHandler>().resources.getColor(R.color.green)
            else -> on<ResourcesHandler>().resources.getColor(R.color.colorPrimary)
        }
    }

    @ColorInt
    fun getLightColor(group: Group): Int {
        return when {
            group.hasEvent() -> on<ResourcesHandler>().resources.getColor(R.color.redLight)
            group.hasPhone() -> on<ResourcesHandler>().resources.getColor(R.color.white)
            group.physical -> on<ResourcesHandler>().resources.getColor(R.color.purpleLight)
            group.ofKind == "quest" -> on<ResourcesHandler>().resources.getColor(R.color.forestgreenLight)
            group.isPublic -> on<ResourcesHandler>().resources.getColor(R.color.greenLight)
            else -> on<ResourcesHandler>().resources.getColor(R.color.colorPrimaryLight)
        }
    }

    @DrawableRes
    fun getColorBackground(group: Group): Int {
        return when {
            group.hasEvent() -> R.drawable.color_red_rounded
            group.hasPhone() -> R.drawable.color_white_rounded
            group.physical -> R.drawable.color_purple_rounded
            group.ofKind == "quest" -> R.drawable.color_forestgreen_rounded
            group.isPublic -> R.drawable.color_green_rounded
            else -> R.drawable.color_primary_rounded
        }
    }

    @DrawableRes
    fun getColorClickable(group: Group): Int {
        return when {
            group.hasEvent() -> R.drawable.clickable_red
            group.hasPhone() -> R.drawable.clickable_white
            group.physical -> R.drawable.clickable_purple
            group.ofKind == "quest" -> R.drawable.clickable_forestgreen
            group.isPublic -> R.drawable.clickable_green
            else -> R.drawable.clickable_blue
        }
    }

    @DrawableRes
    fun getColorClickable8dp(group: Group): Int {
        return when {
            group.hasEvent() -> R.drawable.clickable_red_8dp
            group.hasPhone() -> R.drawable.clickable_white_8dp
            group.physical -> R.drawable.clickable_purple_8dp
            group.ofKind == "quest" -> R.drawable.clickable_forestgreen_8dp
            group.isPublic -> R.drawable.clickable_green_8dp
            else -> R.drawable.clickable_blue_8dp
        }
    }
}
