package closer.vlllage.com.closer.handler.helpers

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

import closer.vlllage.com.closer.R
import com.queatz.on.On
import closer.vlllage.com.closer.store.models.Group

class GroupColorHandler constructor(private val on: On) {

    @ColorInt
    fun getColor(group: Group): Int {
        return if (group.hasEvent()) {
            on<ResourcesHandler>().resources.getColor(R.color.red)
        } else if (group.physical) {
            on<ResourcesHandler>().resources.getColor(R.color.purple)
        } else if (group.isPublic) {
            on<ResourcesHandler>().resources.getColor(R.color.green)
        } else {
            on<ResourcesHandler>().resources.getColor(R.color.colorPrimary)
        }
    }

    @ColorInt
    fun getLightColor(group: Group): Int {
        return if (group.hasEvent()) {
            on<ResourcesHandler>().resources.getColor(R.color.redLight)
        } else if (group.physical) {
            on<ResourcesHandler>().resources.getColor(R.color.purpleLight)
        } else if (group.isPublic) {
            on<ResourcesHandler>().resources.getColor(R.color.greenLight)
        } else {
            on<ResourcesHandler>().resources.getColor(R.color.colorPrimaryLight)
        }
    }

    @DrawableRes
    fun getColorBackground(group: Group): Int {
        return if (group.hasEvent()) {
            R.drawable.color_red_rounded
        } else if (group.physical) {
            R.drawable.color_purple_rounded
        } else if (group.isPublic) {
            R.drawable.color_green_rounded
        } else {
            R.drawable.color_primary_rounded
        }
    }

    @DrawableRes
    fun getColorClickable(group: Group): Int {
        return if (group.hasEvent()) {
            R.drawable.clickable_red
        } else if (group.physical) {
            R.drawable.clickable_purple
        } else if (group.isPublic) {
            R.drawable.clickable_green
        } else {
            R.drawable.clickable_blue
        }
    }
}
