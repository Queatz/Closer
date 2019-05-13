package closer.vlllage.com.closer.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator

import java.lang.Math.max

class RevealAnimator(private val container: MaxSizeFrameLayout, private var initialHeight: Int) {
    private var animator: ValueAnimator? = null
    private var attachListener: View.OnAttachStateChangeListener? = null


    fun cancel() {
        animator?.cancel()
    }

    @JvmOverloads
    fun show(show: Boolean, immediate: Boolean = true) {
        animator?.cancel()

        if (!container.isAttachedToWindow) {
            if (attachListener == null) {
                attachListener = object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(view: View) {
                        show(show, immediate)
                        container.removeOnAttachStateChangeListener(attachListener)
                    }

                    override fun onViewDetachedFromWindow(v: View) {

                    }
                }
            }

            container.addOnAttachStateChangeListener(attachListener)

            return
        }

        if (show) {
            animator = ValueAnimator.ofInt(0, initialHeight)
            animator!!.duration = 500
            animator!!.interpolator = AccelerateDecelerateInterpolator()
            animator!!.startDelay = (if (immediate) 0 else 1700).toLong()
            animator!!.addUpdateListener { animation ->
                container.maxHeight = animation.animatedValue as Int
                container.alpha = animation.animatedFraction
            }
            animator!!.addListener(object : Animator.AnimatorListener {

                private var cancelled: Boolean = false

                override fun onAnimationStart(animation: Animator) {
                    container.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (cancelled) {
                        return
                    }
                    container.maxHeight = MaxSizeFrameLayout.UNSPECIFIED
                }

                override fun onAnimationCancel(animation: Animator) {
                    cancelled = true
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
            animator!!.start()
        } else if (container.visibility != View.GONE) {
            initialHeight = max(initialHeight, container.measuredHeight)
            animator = ValueAnimator.ofInt(container.measuredHeight, 0)
            animator!!.duration = 195
            animator!!.interpolator = DecelerateInterpolator()
            animator!!.addUpdateListener { animation ->
                container.maxHeight = animation.animatedValue as Int
                container.alpha = 1 - animation.animatedFraction
            }
            animator!!.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    container.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    container.visibility = View.GONE
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
            animator!!.start()
        }
    }
}
