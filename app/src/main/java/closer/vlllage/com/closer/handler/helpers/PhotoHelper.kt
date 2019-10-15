package closer.vlllage.com.closer.handler.helpers

import android.widget.ImageView
import androidx.annotation.DimenRes

import com.queatz.on.On
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class PhotoHelper constructor(private val on: On) {
    fun loadCircle(imageView: ImageView, url: String, @DimenRes size: Int? = null) {
        on<ImageHandler>().get().cancelRequest(imageView)
        on<ImageHandler>().get().load(url)
                .noPlaceholder()
                .apply {
                    size?.let {
                        centerCrop()
                        resizeDimen(size, size)
                    }
                }
                .transform(CropCircleTransformation())
                .into(imageView)
    }
}
