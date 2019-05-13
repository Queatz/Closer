package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.Cache
import okhttp3.OkHttpClient

class ImageHandler : PoolMember() {

    override fun onPoolInit() {
        if (picasso == null)
            picasso = Picasso.Builder(`$`(ApplicationHandler::class.java).app)
                    .downloader(OkHttp3Downloader(OkHttpClient.Builder().cache(Cache(`$`(ApplicationHandler::class.java).app.cacheDir, Integer.MAX_VALUE.toLong())).build()))
                    .build()
    }

    fun get() = picasso!!

    companion object {
        private var picasso: Picasso? = null
    }
}
