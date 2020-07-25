package closer.vlllage.com.closer.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView


class ScalingImageView : AppCompatImageView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        var widthMeasureSpec = widthMeasureSpec

        if (drawable != null) {
            val mDrawableWidth = drawable.intrinsicWidth
            val mDrawableHeight = drawable.intrinsicHeight
            val actualAspect = mDrawableWidth.toFloat() / mDrawableHeight.toFloat()

            // Assuming the width is ok, so we calculate the height.
            val actualWidth = MeasureSpec.getSize(widthMeasureSpec)
            var height = (actualWidth / actualAspect).toInt()

            if (height > maxHeight) {
                height = maxHeight
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((actualWidth * actualAspect).toInt(), MeasureSpec.EXACTLY)
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}