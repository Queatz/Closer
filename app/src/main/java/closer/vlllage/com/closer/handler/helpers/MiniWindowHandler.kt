package closer.vlllage.com.closer.handler.helpers

import android.support.constraint.ConstraintLayout
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting.CLOSER_SETTINGS_OPEN_GROUP_EXPANDED
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.ui.DragTriggerView
import closer.vlllage.com.closer.ui.TimedValue
import java.lang.Math.abs
import java.lang.Math.max

class MiniWindowHandler : PoolMember() {

    fun attach(toggleView: View, windowView: View, miniWindowEventListener: (() -> Unit)?) {

        val miniWindowHeight = `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.miniWindowHeight)
        val params = windowView.layoutParams as ConstraintLayout.LayoutParams
        val miniWindowMinTopMargin = `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.miniWindowMinTopMargin)
        val miniWindowTopMargin = `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.miniWindowTopMargin)

        windowView.clipToOutline = true

        toggleView.isClickable = true
        DragTriggerView(toggleView, object : DragTriggerView.OnDragEventListener {

            private var dead: Boolean = false

            private var startMaxHeight = 0
            private var startY = 0.0

            override fun onDragStart(x: TimedValue<Float>, y: TimedValue<Float>) {
                if (dead) return

                startMaxHeight = windowView.measuredHeight
                params.topMargin = miniWindowMinTopMargin
                windowView.layoutParams = params
                startY = y.get()!!.toDouble()
            }

            override fun onDragRelease(x: TimedValue<Float>, y: TimedValue<Float>) {
                if (dead) return

                val startMaxHeight = params.matchConstraintMaxHeight
                val startTopMargin = params.topMargin

                val yVelocity = (y.now()!! - y.get()!!).toDouble()

                if (abs(yVelocity) < 10) {
                    return
                }

                val animation = object : Animation() {
                    override fun willChangeBounds(): Boolean {
                        return true
                    }

                    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                        if (yVelocity > 0) {
                            params.matchConstraintMaxHeight = mix(startMaxHeight.toFloat(), miniWindowHeight.toFloat(), interpolatedTime).toInt()
                            params.topMargin = mix(startTopMargin.toFloat(), miniWindowTopMargin.toFloat(), interpolatedTime).toInt()
                        } else {
                            params.matchConstraintMaxHeight = mix(startMaxHeight.toFloat(), (miniWindowHeight * 3).toFloat(), interpolatedTime).toInt()
                            params.topMargin = mix(startTopMargin.toFloat(), miniWindowMinTopMargin.toFloat(), interpolatedTime).toInt()
                        }
                        windowView.layoutParams = params
                    }
                }

                animation.duration = 225
                animation.interpolator = AccelerateDecelerateInterpolator()
                windowView.startAnimation(animation)
            }

            override fun onDragUpdate(x: TimedValue<Float>, y: TimedValue<Float>) {
                if (dead) return

                val currentMiniWindowHeight = (startMaxHeight + (startY - y.get()!!)).toInt()

                if (startY < y.get()!! && currentMiniWindowHeight - miniWindowHeight < -CLOSE_TUG_SLOP && miniWindowEventListener != null) {
                    dead = true
                    miniWindowEventListener.invoke()
                }

                params.matchConstraintMaxHeight = max(miniWindowHeight, currentMiniWindowHeight)
                windowView.layoutParams = params
            }
        })

        if (`$`(SettingsHandler::class.java).get(CLOSER_SETTINGS_OPEN_GROUP_EXPANDED)) {
            val startMaxHeight = params.matchConstraintMaxHeight
            val startTopMargin = params.topMargin

            val animation = object : Animation() {
                override fun willChangeBounds(): Boolean {
                    return true
                }

                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    params.matchConstraintMaxHeight = mix(startMaxHeight.toFloat(), (miniWindowHeight * 3).toFloat(), interpolatedTime).toInt()
                    params.topMargin = mix(startTopMargin.toFloat(), miniWindowMinTopMargin.toFloat(), interpolatedTime).toInt()
                    windowView.layoutParams = params
                }
            }

            animation.duration = 225
            animation.interpolator = AccelerateDecelerateInterpolator()
            windowView.startAnimation(animation)
        }
    }

    private fun mix(a: Float, b: Float, v: Float): Float {
        return a * (1 - v) + b * v
    }

    companion object {

        private const val CLOSE_TUG_SLOP = 32
    }
}
