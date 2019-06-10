package closer.vlllage.com.closer.handler.helpers

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import closer.vlllage.com.closer.R
import com.queatz.on.On
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class LightDarkHandler constructor(private val on: On) {

    private val DARK = LightDarkColors(
            on<ResourcesHandler>().resources.getColor(R.color.text),
            on<ResourcesHandler>().resources.getColor(R.color.textHint),
        ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.text)),
            R.drawable.clickable_light_flat,
            R.drawable.clickable_light_rounded_8dp,
            R.drawable.clickable_light
    )

    private val LIGHT = LightDarkColors(
            on<ResourcesHandler>().resources.getColor(R.color.textInverse),
            on<ResourcesHandler>().resources.getColor(R.color.textHintInverse),
            ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.textInverse)),
            R.drawable.clickable_dark_flat,
            R.drawable.clickable_dark_rounded_8dp,
            R.drawable.clickable_accent_flat
    )

    val onLightChanged: Observable<LightDarkColors> = BehaviorSubject.createDefault(DARK)

    fun setLight(isLight: Boolean) {
        (onLightChanged as BehaviorSubject).onNext(if (isLight) LIGHT else DARK)
    }
}

data class LightDarkColors constructor(
        @ColorInt val text: Int,
        @ColorInt val hint: Int,
        val tint: ColorStateList,
        @DrawableRes val clickableRoundedBackground: Int,
        @DrawableRes val clickableRoundedBackground8dp: Int,
        @DrawableRes val clickableBackground: Int
)