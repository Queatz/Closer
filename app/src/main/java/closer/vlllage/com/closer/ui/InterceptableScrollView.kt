package closer.vlllage.com.closer.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class InterceptableScrollView : ScrollView {

    private var onInterceptTouchListener: OnTouchListener? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context) : super(context) {}

    fun setOnInterceptTouchListener(onInterceptTouchListener: OnTouchListener) {
        this.onInterceptTouchListener = onInterceptTouchListener
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (onInterceptTouchListener != null) {
            if (onInterceptTouchListener!!.onTouch(this, event)) {
                return true
            }
        }

        return super.onInterceptTouchEvent(event)
    }
}
