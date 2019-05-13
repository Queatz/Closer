package closer.vlllage.com.closer.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import closer.vlllage.com.closer.R

class MaxSizeFrameLayout : FrameLayout {

    var maxHeight: Int = 0
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    var maxWidth: Int = 0
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }

        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.MaxSizeFrameLayout,
                0, 0)

        try {
            maxHeight = typedArray.getDimensionPixelSize(R.styleable.MaxSizeFrameLayout_maxHeight, -1)
            maxWidth = typedArray.getDimensionPixelSize(R.styleable.MaxSizeFrameLayout_maxWidth, -1)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        if (maxHeight >= 0) {
            heightMeasureSpec = calculate(heightMeasureSpec, maxHeight)
        }

        if (maxWidth >= 0) {
            widthMeasureSpec = calculate(widthMeasureSpec, maxWidth)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }

        return false
    }

    private fun calculate(measureSpec: Int, max: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)
        val mode = MeasureSpec.getMode(measureSpec)

        when (mode) {
            MeasureSpec.AT_MOST -> return MeasureSpec.makeMeasureSpec(Math.min(size, max), MeasureSpec.AT_MOST)
            MeasureSpec.UNSPECIFIED -> return MeasureSpec.makeMeasureSpec(max, MeasureSpec.AT_MOST)
            MeasureSpec.EXACTLY -> return MeasureSpec.makeMeasureSpec(Math.min(size, max), MeasureSpec.EXACTLY)
            else -> return measureSpec
        }
    }

    companion object {

        val UNSPECIFIED = -1
    }
}
