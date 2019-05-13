package closer.vlllage.com.closer.ui

import android.animation.Animator
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.ViewTreeObserver
import android.view.animation.OvershootInterpolator
import closer.vlllage.com.closer.R
import java.lang.Math.*
import java.util.*

class DraggableView(private val view: View, private val container: View) {

    private var positionBeforeKeyboardOpenedX: Float? = null
    private var positionBeforeKeyboardOpenedY: Float? = null
    private var moveToBottom: Boolean = false
    private var dragStartTime = Date()
    private var centerAnimation: ViewPropertyAnimator? = null

    init {

        view.isClickable = true
        view.setOnTouchListener(object : View.OnTouchListener {
            private var xDiffInTouchPointAndViewTopLeftCorner: Float = 0.toFloat()
            private var yDiffInTouchPointAndViewTopLeftCorner: Float = 0.toFloat()

            private val trackedX = TimedValue<Float>(75)
            private val trackedY = TimedValue<Float>(75)

            private val isSingleTap: Boolean
                get() = Date().time - dragStartTime.time < SINGLE_TAP_CONFIRM_TIME_MS

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.clearAnimation()
                        xDiffInTouchPointAndViewTopLeftCorner = motionEvent.rawX - view.x
                        yDiffInTouchPointAndViewTopLeftCorner = motionEvent.rawY - view.y
                        dragStartTime = Date()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        view.x = clampX((motionEvent.rawX - xDiffInTouchPointAndViewTopLeftCorner).toDouble())
                        view.y = clampY((motionEvent.rawY - yDiffInTouchPointAndViewTopLeftCorner).toDouble())
                        trackedX.report(motionEvent.rawX)
                        trackedY.report(motionEvent.rawY)
                        positionBeforeKeyboardOpenedX = null
                        positionBeforeKeyboardOpenedY = null
                    }
                    MotionEvent.ACTION_UP -> {
                        val fromX = trackedX.get()
                        val fromY = trackedY.get()

                        if (fromX != null && fromY != null) {
                            val velocity = hypot((motionEvent.rawX - fromX).toDouble(), (motionEvent.rawY - fromY).toDouble())

                            if (velocity > SINGLE_TAP_CONFIRM_MAX_VELOCITY) {
                                val angle = atan2((motionEvent.rawY - fromY).toDouble(), (motionEvent.rawX - fromX).toDouble())
                                animate(clampXForAnimation(view.x + 5.0 * velocity * cos(angle)), clampY(view.y + velocity * sin(angle)))
                            } else if (isSingleTap) {
                                center()
                            }
                        } else if (isSingleTap) {
                            center()
                        }
                    }
                }

                return true
            }
        })

        container.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

            private var previousHeight = 0

            override fun onGlobalLayout() {
                if (moveToBottom) {
                    moveToBottom = false
                    view.x = clampX(container.width.toDouble())
                    view.y = clampY((container.height / 3 - view.height / 2).toDouble())
                    return
                }

                val h = container.height - container.paddingBottom

                if (view.findFocus() != null && previousHeight > h) {
                    if (positionBeforeKeyboardOpenedX == null && positionBeforeKeyboardOpenedY == null) {
                        positionBeforeKeyboardOpenedX = view.x
                        positionBeforeKeyboardOpenedY = view.y
                    }
                    animate((container.width / 2 - view.width / 2).toFloat(), (h - view.height - view.paddingBottom).toFloat())
                } else if (previousHeight <= h && positionBeforeKeyboardOpenedX != null && positionBeforeKeyboardOpenedY != null) {
                    animate(clampX(positionBeforeKeyboardOpenedX!!.toDouble()), clampY(positionBeforeKeyboardOpenedY!!.toDouble()))

                    positionBeforeKeyboardOpenedX = null
                    positionBeforeKeyboardOpenedY = null
                } else if (clampX(view.x.toDouble()) != view.x || clampY(view.x.toDouble()) != view.y) {
                    animate(clampX(view.x.toDouble()), clampY(view.y.toDouble()))
                }

                previousHeight = container.height
            }
        })
    }

    private fun animate(x: Float, y: Float) {
        if (centerAnimation != null) {
            return
        }

        view.animate()
                .x(x)
                .y(y)
                .setInterpolator(OvershootInterpolator())
                .setDuration(225)
                .start()
    }

    fun center() {
        centerAnimation = view.animate()
                .x((container.width / 2 - view.width / 2).toFloat())
                .y((container.height / 2 - view.height / 2).toFloat())
                .setInterpolator(OvershootInterpolator())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        centerAnimation = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        centerAnimation = null
                    }

                    override fun onAnimationRepeat(animation: Animator) {

                    }
                })
                .setDuration(225)
        centerAnimation!!.start()
    }

    fun moveToBottom() {
        moveToBottom = true
    }

    private fun clampXForAnimation(x: Double): Float {
        val m = view.context.resources.getDimensionPixelSize(R.dimen.padDouble) * 2
        val w = container.width

        return if (x + view.width < w / 3) {
            (-view.width + m).toFloat()
        } else if (x > w / 3 * 2) {
            (w - m).toFloat()
        } else {
            (w / 2 - view.width / 2).toFloat()
        }
    }

    private fun clampX(x: Double): Float {
        val m = view.context.resources.getDimensionPixelSize(R.dimen.padDouble) * 2
        return max((-view.width + m).toDouble(), min((container.width - m).toDouble(), x)).toFloat()
    }

    private fun clampY(y: Double): Float {
        val m = view.context.resources.getDimensionPixelSize(R.dimen.padDouble) * 2
        return max((-view.height + m).toDouble(), min((container.height - m).toDouble(), y)).toFloat()
    }

    companion object {

        private val SINGLE_TAP_CONFIRM_TIME_MS = 100
        private val SINGLE_TAP_CONFIRM_MAX_VELOCITY = 2
    }
}
