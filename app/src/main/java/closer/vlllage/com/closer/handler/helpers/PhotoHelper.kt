package closer.vlllage.com.closer.handler.helpers

import android.widget.ImageView
import androidx.annotation.DimenRes
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.queatz.on.On

class PhotoHelper constructor(private val on: On) {
    fun loadCircle(imageView: ImageView, url: String, @DimenRes size: Int? = null) {
        on<ImageHandler>().get().clear(imageView)
        on<ImageHandler>().get().load(url)
                .apply(RequestOptions().also {
                    size?.let { size -> it.override(size) }
                }.centerCrop().circleCrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
    }
}
