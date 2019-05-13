package closer.vlllage.com.closer.handler.helpers

import android.widget.ImageView

import closer.vlllage.com.closer.pool.PoolMember
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class PhotoHelper : PoolMember() {
    fun loadCircle(imageView: ImageView, url: String) {
        `$`(ImageHandler::class.java).get()!!.cancelRequest(imageView)
        `$`(ImageHandler::class.java).get()!!.load(url)
                .noPlaceholder()
                .transform(CropCircleTransformation())
                .into(imageView)
    }
}
