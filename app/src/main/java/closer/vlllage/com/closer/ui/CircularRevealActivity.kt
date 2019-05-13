package closer.vlllage.com.closer.ui

import android.animation.Animator
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator

import closer.vlllage.com.closer.pool.PoolActivity

abstract class CircularRevealActivity : PoolActivity() {
    private var sourceBounds: Rect? = null
    private var finishCallback: (() -> Unit)? = null
    private var finishAnimator: Animator? = null
    private var background: View? = null

    protected abstract val backgroundId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        background = findViewById(backgroundId)

        sourceBounds = intent.sourceBounds

        val viewTreeObserver = background!!.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                background!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                reveal()
            }
        })

        background!!.setOnTouchListener { view, motionEvent ->
            background!!.setOnTouchListener(null)
            finish()
            true
        }
    }

    protected fun reveal() {
        if (sourceBounds == null) {
            val rect = Rect()
            window.decorView.getGlobalVisibleRect(rect)
            rect.top = rect.bottom
            sourceBounds = rect
        }

        val animator = ViewAnimationUtils.createCircularReveal(background,
                sourceBounds!!.centerX(),
                sourceBounds!!.centerY(),
                0f,
                Math.hypot(this@CircularRevealActivity.window.decorView.width.toDouble(), this@CircularRevealActivity.window.decorView.height.toDouble()).toFloat()
        )
        animator.duration = 225
        animator.interpolator = AccelerateInterpolator()
        animator.start()

        window.decorView.alpha = 0f
        window.decorView.animate()
                .alpha(1f)
                .setDuration(225)
                .setInterpolator(AccelerateInterpolator())
                .start()
    }

    protected fun setSourceBounds(sourceBounds: Rect?) {
        this.sourceBounds = sourceBounds
    }

    override fun finish() {
        if (sourceBounds == null) {
            super.finish()
            return
        }

        if (finishAnimator != null && finishAnimator!!.isRunning) {
            return
        }

        finishAnimator = ViewAnimationUtils.createCircularReveal(findViewById(backgroundId),
                sourceBounds!!.centerX(),
                sourceBounds!!.centerY(),
                Math.hypot(window.decorView.width.toDouble(), window.decorView.height.toDouble()).toFloat(),
                0f
        )
        finishAnimator!!.duration = 195
        finishAnimator!!.interpolator = AccelerateInterpolator(0.5f)
        finishAnimator!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                super@CircularRevealActivity.finish()
                window.decorView.visibility = View.GONE
                overridePendingTransition(0, 0)

                finishCallback?.invoke()
            }

            override fun onAnimationCancel(animator: Animator) {
                super@CircularRevealActivity.finish()
                window.decorView.visibility = View.GONE
                overridePendingTransition(0, 0)

                finishCallback?.invoke()
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })
        finishAnimator!!.start()

        window.decorView.animate()
                .alpha(0f)
                .setDuration(195)
                .setInterpolator(AccelerateInterpolator(0.5f))
                .start()
    }

    fun finish(callback: () -> Unit) {
        finishCallback = callback
        finish()
    }
}
