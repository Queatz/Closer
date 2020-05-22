package closer.vlllage.com.closer.handler.helpers

import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.queatz.on.On
import com.queatz.on.OnLifecycle

class ImageHandler constructor(private val on: On) : OnLifecycle {

    override fun on() {
        if (glide == null)
            glide = Glide.with(on<ApplicationHandler>().app).setDefaultRequestOptions(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
    }

    fun get() = glide!!

    companion object {
        private var glide: RequestManager? = null
    }
}
