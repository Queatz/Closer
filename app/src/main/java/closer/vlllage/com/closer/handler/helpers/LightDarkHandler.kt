package closer.vlllage.com.closer.handler.helpers

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import closer.vlllage.com.closer.R
import com.queatz.on.On
import io.reactivex.rxjava3.subjects.BehaviorSubject

class LightDarkHandler constructor(private val on: On) {

    val DARK = LightDarkColors(false,
            on<ResourcesHandler>().resources.getColor(R.color.text),
            on<ResourcesHandler>().resources.getColor(R.color.white_50),
            on<ResourcesHandler>().resources.getColor(R.color.white_50),
            on<ResourcesHandler>().resources.getColor(R.color.textHint),
        ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.icon)),
        ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.white_50)),
            R.drawable.clickable_light_flat,
            R.drawable.clickable_light_50_rounded_512dp,
            R.drawable.clickable_light_rounded_8dp,
            R.drawable.clickable_light,
            R.drawable.clickable_accent_rounded,
            R.drawable.clickable_borderless_light
    )

    val LIGHT = LightDarkColors(true,
            on<ResourcesHandler>().resources.getColor(R.color.textInverse),
            on<ResourcesHandler>().resources.getColor(R.color.colorPrimary),
            on<ResourcesHandler>().resources.getColor(R.color.colorAccentDark),
            on<ResourcesHandler>().resources.getColor(R.color.textHintInverse),
            ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.iconInverse)),
            ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.colorPrimary)),
            R.drawable.clickable_dark_flat,
            R.drawable.clickable_dark_50_rounded_512dp,
            R.drawable.clickable_dark_rounded_8dp,
            R.drawable.clickable_accent_flat,
            R.drawable.clickable_accent_rounded,
            R.drawable.clickable_borderless_dark
    )

    val onLightChanged = BehaviorSubject.createDefault(DARK)

    fun setLight(isLight: Boolean) {
        onLightChanged.onNext(if (isLight) LIGHT else DARK)
    }

    fun get() = onLightChanged.value!!

    fun isLight() = onLightChanged.value == LIGHT
}

data class LightDarkColors constructor(
        val light: Boolean,
        @ColorInt val text: Int,
        @ColorInt val selected: Int,
        @ColorInt val action: Int,
        @ColorInt val hint: Int,
        val tint: ColorStateList,
        val tintSelected: ColorStateList,
        @DrawableRes val clickableRoundedBackground: Int,
        @DrawableRes val clickableRoundedBackgroundLight: Int,
        @DrawableRes val clickableRoundedBackground8dp: Int,
        @DrawableRes val clickableBackground: Int,
        @DrawableRes val clickableRoundedBackgroundAccent: Int,
        @DrawableRes val clickableRoundedBackgroundBorderless: Int
)