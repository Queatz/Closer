package closer.vlllage.com.closer.ui

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import closer.vlllage.com.closer.extensions.visible

import com.queatz.on.On

class Animate constructor(private val on: On) {
    fun alpha(view: View, show: Boolean) {
        view.clearAnimation()

        if (show) {
            if (view.visibility == View.GONE) {
                val animation = AlphaAnimation(0f, 1f)
                animation.duration = 195
                view.startAnimation(animation)
                view.visible = true
            } else {
                val animation = AlphaAnimation(view.alpha, 1f)
                animation.duration = 195
                view.startAnimation(animation)
            }
        } else {
            if (view.visibility == View.GONE) {

            } else {
                val animation = AlphaAnimation(view.alpha, 0f)
                animation.duration = 225
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        view.visible = false
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
                view.startAnimation(animation)
            }
        }
    }
}
