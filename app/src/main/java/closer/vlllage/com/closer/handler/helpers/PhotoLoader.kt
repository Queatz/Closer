package closer.vlllage.com.closer.handler.helpers

import android.widget.ImageView
import closer.vlllage.com.closer.pool.PoolMember
import com.squareup.picasso.Callback
import jp.wasabeef.picasso.transformations.BlurTransformation

class PhotoLoader : PoolMember() {

    fun softLoad(photoUrl: String, imageView: ImageView) {
        `$`(ImageHandler::class.java).get()!!.load("$photoUrl?s=32")
                .noPlaceholder()
                .transform(BlurTransformation(imageView.context, 2))
                .into(imageView, object : Callback {
                    override fun onSuccess() {
                        `$`(ImageHandler::class.java).get()!!.load("$photoUrl?s=512").noPlaceholder().into(imageView)
                    }

                    override fun onError(e: Exception) {
                        e.printStackTrace()
                        onSuccess()
                    }
                })
    }
}
