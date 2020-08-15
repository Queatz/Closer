package closer.vlllage.com.closer.handler.helpers

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.queatz.on.On
import jp.wasabeef.glide.transformations.BlurTransformation

class PhotoLoader constructor(private val on: On) {

    fun softLoad(photoUrl: String, imageView: ImageView) {
        on<ImageHandler>().get().load("$photoUrl?s=32")
                .apply(RequestOptions().transform(BlurTransformation(2)))
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        e?.printStackTrace()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        on<TimerHandler>().post {
                            on<ImageHandler>().get().load("$photoUrl?s=512").transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
                        }
                        return false
                    }

                })
                .into(imageView)
    }
}
