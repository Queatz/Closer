package closer.vlllage.com.closer.ui

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R

class FloatingRecyclerView : RecyclerView {
    private var isScrolling: Boolean = false
    private var colorAnimation: ValueAnimator? = null
    private var isSolidBackground: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        setBackgroundResource(android.R.color.transparent)
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if ((layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() > 0) {
                    if (!isSolidBackground) {
                        isSolidBackground = true
                        animateBackground(recyclerView, R.color.offwhite)
                    }
                } else if (isSolidBackground) {
                    isSolidBackground = false
                    animateBackground(recyclerView, R.color.offwhite_transparent)
                }
            }
        })
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        if (!isInLayout && !isSolidBackground && e.action == MotionEvent.ACTION_DOWN) {
            isLayoutFrozen = false
        }

        return super.onInterceptTouchEvent(e)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!isInLayout && !isSolidBackground && e.action == MotionEvent.ACTION_DOWN) {
            isLayoutFrozen = !isScrolling
        }

        return super.onTouchEvent(e)
    }

    override fun onScrollStateChanged(state: Int) {
        isScrolling = state != SCROLL_STATE_IDLE
    }

    private fun animateBackground(recyclerView: RecyclerView, @ColorRes color: Int) {
        if (colorAnimation != null) {
            colorAnimation!!.end()
        }

        colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(),
                (recyclerView.background as ColorDrawable).color,
                resources.getColor(color)).apply {
            duration = 225
            addUpdateListener { animator -> setBackgroundColor(animator.animatedValue as Int) }
            start()
        }
    }
}
