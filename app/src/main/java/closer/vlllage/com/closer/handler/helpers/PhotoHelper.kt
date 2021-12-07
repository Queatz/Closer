package closer.vlllage.com.closer.handler.helpers

import android.graphics.Color
import android.widget.ImageView
import androidx.annotation.DimenRes
import closer.vlllage.com.closer.store.models.Phone
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.queatz.on.On

class PhotoHelper constructor(private val on: On) {
    fun colorForPhone(phoneId: String) = Color.HSVToColor(floatArrayOf(phoneId.hashCode().toDouble().mod(360.0).toFloat(), .66f, .66f))

    fun loadCircle(imageView: ImageView, url: String, @DimenRes sizeRes: Int? = null) {
        on<ImageHandler>().get().clear(imageView)
        on<ImageHandler>().get().load(url)
                .apply(RequestOptions().also {
                    sizeRes?.let { size -> it.override(on<ResourcesHandler>().resources.getDimensionPixelSize(size)) }
                }.centerCrop().circleCrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
    }
}
