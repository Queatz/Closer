package closer.vlllage.com.closer.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet

import com.github.chrisbanes.photoview.PhotoView

class StablePhotoView : PhotoView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attr: AttributeSet) : super(context, attr)

    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(context, attr, defStyle)

    override fun setImageDrawable(drawable: Drawable?) {
        val imageMatrix = Matrix()
        getSuppMatrix(imageMatrix)
        super.setImageDrawable(drawable)
        setSuppMatrix(imageMatrix)
    }
}
