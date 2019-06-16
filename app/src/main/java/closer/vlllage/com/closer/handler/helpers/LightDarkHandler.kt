package closer.vlllage.com.closer.handler.helpers

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import closer.vlllage.com.closer.R
import com.queatz.on.On
import io.reactivex.subjects.BehaviorSubject

class LightDarkHandler constructor(private val on: On) {

    private val DARK = LightDarkColors(
            on<ResourcesHandler>().resources.getColor(R.color.text),
            on<ResourcesHandler>().resources.getColor(R.color.white_50),
            on<ResourcesHandler>().resources.getColor(R.color.textHint),
        ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.text)),
        ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.white_50)),
            R.drawable.clickable_light_flat,
            R.drawable.clickable_light_rounded_8dp,
            R.drawable.clickable_light
    )

    private val LIGHT = LightDarkColors(
            on<ResourcesHandler>().resources.getColor(R.color.textInverse),
            on<ResourcesHandler>().resources.getColor(R.color.colorPrimary),
            on<ResourcesHandler>().resources.getColor(R.color.textHintInverse),
            ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.textInverse)),
            ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary)),
            R.drawable.clickable_dark_flat,
            R.drawable.clickable_dark_rounded_8dp,
            R.drawable.clickable_accent_flat
    )

    val onLightChanged = BehaviorSubject.createDefault(DARK)

    fun setLight(isLight: Boolean) {
        (onLightChanged as BehaviorSubject).onNext(if (isLight) LIGHT else DARK)
    }
}

data class LightDarkColors constructor(
        @ColorInt val text: Int,
        @ColorInt val selected: Int,
        @ColorInt val hint: Int,
        val tint: ColorStateList,
        val tintSelected: ColorStateList,
        @DrawableRes val clickableRoundedBackground: Int,
        @DrawableRes val clickableRoundedBackground8dp: Int,
        @DrawableRes val clickableBackground: Int
)