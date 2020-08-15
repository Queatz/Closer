package closer.vlllage.com.closer.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnAttach
import closer.vlllage.com.closer.extensions.visible


class RevealAnimatorForConstraintLayout(private val container: ConstraintLayout, private var initialHeight: Int) {
    private var animator: ValueAnimator? = null
    private var animatorIsHiding: Boolean = false

    fun cancel() {
        animator?.removeAllListeners()
        animator?.cancel()
        animator = null
    }

    fun show(show: Boolean, immediate: Boolean = true) {
        container.doOnAttach {
            if (show) {
                if (!animatorIsHiding) cancel()
                val preexistingAnimator = animator
                animatorIsHiding = false
                animator = ValueAnimator.ofInt(0, initialHeight).apply {
                    duration = 500
                    interpolator = AccelerateDecelerateInterpolator()
                    startDelay = (if (immediate) 0 else 4000).toLong()
                    addUpdateListener { animation ->
                        container.maxHeight = animation.animatedValue as Int
                        container.alpha = animation.animatedFraction
                    }
                    addListener(object : Animator.AnimatorListener {

                        private var cancelled: Boolean = false

                        override fun onAnimationStart(animation: Animator) {
                            preexistingAnimator?.removeAllListeners()
                            preexistingAnimator?.cancel()
                            if (cancelled) return
                            container.visible = true
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            if (cancelled) return
                            container.maxHeight = Int.MAX_VALUE
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            cancelled = true
                        }

                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                    start()
                }
            } else if (container.visible) {
                cancel()
                initialHeight = initialHeight.coerceAtLeast(container.measuredHeight)
                animatorIsHiding = true
                animator = ValueAnimator.ofInt(container.measuredHeight, 0).apply {
                    duration = 195
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { animation ->
                        container.maxHeight = animation.animatedValue as Int
                        container.alpha = 1 - animation.animatedFraction
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            container.visible = true
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            container.visible = false
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            container.visible = false
                        }

                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                    start()
                }
            } else {
                cancel()
            }
        }
    }
}
