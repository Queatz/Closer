package closer.vlllage.com.closer.handler.helpers

import android.widget.ImageView

import com.queatz.on.On
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class PhotoHelper constructor(private val on: On) {
    fun loadCircle(imageView: ImageView, url: String) {
        on<ImageHandler>().get().cancelRequest(imageView)
        on<ImageHandler>().get().load(url)
                .noPlaceholder()
                .transform(CropCircleTransformation())
                .into(imageView)
    }
}
