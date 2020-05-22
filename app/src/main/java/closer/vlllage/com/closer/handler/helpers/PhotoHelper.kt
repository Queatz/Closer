package closer.vlllage.com.closer.handler.helpers

import android.widget.ImageView
import androidx.annotation.DimenRes
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.queatz.on.On

class PhotoHelper constructor(private val on: On) {
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
