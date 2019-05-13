package closer.vlllage.com.closer.ui

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

import java.lang.Math.abs

class SoftScrollStopper : FrameLayout {

    private var isChildScrolling: Boolean = false
    private val originPosition = Point()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> isChildScrolling = false
            MotionEvent.ACTION_DOWN -> {
                originPosition.x = event.rawX.toInt()
                originPosition.y = event.rawY.toInt()
                isChildScrolling = true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = (event.rawX - originPosition.x).toInt()
                val deltaY = (event.rawY - originPosition.y).toInt()

                if (abs(deltaX) > SLOP_RADIUS || abs(deltaY) > SLOP_RADIUS) {
                    isChildScrolling = if (abs(deltaX) > abs(deltaY)) {
                        if (deltaX > 0 && getChildAt(0).canScrollHorizontally(-1)) {
                            true
                        } else
                            deltaX < 0 && getChildAt(0).canScrollHorizontally(1)
                    } else {
                        false
                    }
                }
            }
        }

        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(isChildScrolling)
        }

        return false
    }

    companion object {
        private const val SLOP_RADIUS = 16
    }
}
