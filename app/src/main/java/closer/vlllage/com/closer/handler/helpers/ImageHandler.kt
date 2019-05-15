package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On
import com.queatz.on.OnLifecycle
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.Cache
import okhttp3.OkHttpClient

class ImageHandler constructor(private val on: On) : OnLifecycle {

    override fun on() {
        if (picasso == null)
            picasso = Picasso.Builder(on<ApplicationHandler>().app)
                    .downloader(OkHttp3Downloader(OkHttpClient.Builder().cache(Cache(on<ApplicationHandler>().app.cacheDir, Integer.MAX_VALUE.toLong())).build()))
                    .build()
    }

    fun get() = picasso!!

    companion object {
        private var picasso: Picasso? = null
    }
}
