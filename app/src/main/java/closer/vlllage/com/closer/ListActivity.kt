package closer.vlllage.com.closer

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolActivity
import kotlinx.android.synthetic.main.activity_share.*

abstract class ListActivity : PoolActivity() {

    protected lateinit var recyclerView: RecyclerView

    private var finishAnimator: TranslateAnimation? = null
    private var finishCallback: Runnable? = null

    protected var closeCallback: (() -> Boolean)? = null

    protected val isDone: Boolean
        get() = finishAnimator != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_share)

        background.clipToOutline = true
        recyclerView = shareRecyclerView

        recyclerView.layoutManager = LinearLayoutManager(this)

        background.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                if (closeCallback?.invoke() != false) {
                    background.setOnTouchListener(null)
                    finish()
                }
                true
            } else false
        }

        val viewTreeObserver = background.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                background.viewTreeObserver.removeOnGlobalLayoutListener(this)
                reveal()
            }

            private fun reveal() {
                val colorAnim = ObjectAnimator.ofInt(background, "backgroundColor", getColor(R.color.black_transparent), getColor(R.color.black_10))
                colorAnim.duration = 225
                colorAnim.setEvaluator(ArgbEvaluator())
                colorAnim.start()

                val yPercent = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.feedPeekHeight) * 2 / recyclerView.measuredHeight.toFloat()
                val animation = TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, yPercent, Animation.RELATIVE_TO_PARENT, 0f
                )
                animation.duration = 225
                animation.interpolator = DecelerateInterpolator()
                recyclerView.startAnimation(animation)
            }
        })
    }

    override fun finish() {
        if (finishAnimator != null) {
            return
        }

        val colorAnim = ObjectAnimator.ofInt(background, "backgroundColor", getColor(R.color.black_10), getColor(R.color.black_transparent))
        colorAnim.duration = 195
        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.start()

        finishAnimator = TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 1f
        )
        finishAnimator!!.duration = 195
        finishAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        finishAnimator!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                super@ListActivity.finish()
                window.decorView.visible = false
                overridePendingTransition(0, 0)
                finishAnimator = null

                finishCallback?.run()
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        recyclerView.startAnimation(finishAnimator)
    }

    fun finish(callback: Runnable) {
        finishCallback = callback
        finish()
    }
}
