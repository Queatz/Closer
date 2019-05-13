package closer.vlllage.com.closer.handler.media

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import closer.vlllage.com.closer.pool.PoolMember

class PlaceholderHandler : PoolMember() {
    fun getHeightFromAspectRatio(width: Float, aspectRatio: Float): Int {
        return (width / aspectRatio).toInt()
    }

    fun drawableFromBase64(resources: Resources, image: String): BitmapDrawable {
        val bytes = Base64.decode(image, Base64.DEFAULT)

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return BitmapDrawable(resources, bitmap)
    }

}
