package closer.vlllage.com.closer.handler.helpers

import android.widget.ImageView
import com.queatz.on.On
import com.squareup.picasso.Callback
import jp.wasabeef.picasso.transformations.BlurTransformation

class PhotoLoader constructor(private val on: On) {

    fun softLoad(photoUrl: String, imageView: ImageView) {
        on<ImageHandler>().get().load("$photoUrl?s=32")
                .noPlaceholder()
                .transform(BlurTransformation(imageView.context, 2))
                .into(imageView, object : Callback {
                    override fun onSuccess() {
                        on<ImageHandler>().get().load("$photoUrl?s=512").noPlaceholder().into(imageView)
                    }

                    override fun onError(e: Exception) {
                        e.printStackTrace()
                        onSuccess()
                    }
                })
    }
}
