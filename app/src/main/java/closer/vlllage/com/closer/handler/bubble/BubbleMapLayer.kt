package closer.vlllage.com.closer.handler.bubble

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import closer.vlllage.com.closer.handler.map.MapHandler.Companion.DEFAULT_ZOOM
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlin.math.pow

/**
 * Created by jacob on 2/18/18.
 */

class BubbleMapLayer {

    val mapBubbles = HashSet<MapBubble>()
    private val mapBubbleAnimations = HashMap<MapBubble, Animator>()
    private val mapBubbleAppearDisappearAnimations = HashMap<MapBubble, ViewPropertyAnimator>()
    private var map: GoogleMap? = null
    private var view: ViewGroup? = null
    private var bubbleView: BubbleView? = null

    fun attach(view: ViewGroup, bubbleView: BubbleView) {
        this.view = view
        this.bubbleView = bubbleView
    }

    fun attach(map: GoogleMap) {
        this.map = map
        update()
    }

    fun add(mapBubble: MapBubble) {
        if (mapBubbles.contains(mapBubble)) {
            return
        }

        mapBubbles.add(mapBubble)
        mapBubble.view = bubbleView!!.createView(view, mapBubble)
        mapBubble.onViewReadyListener?.invoke(mapBubble.view!!)
        view!!.addView(mapBubble.view)

        if (map == null) {
            view!!.post { update(mapBubble) }
            return
        }

        mapBubble.view!!.scaleX = 0f
        mapBubble.view!!.scaleY = 0f
        val animator = mapBubble.view!!.animate()
                .scaleX(zoomScale(mapBubble))
                .scaleY(zoomScale(mapBubble))
                .setDuration(150)
                .setInterpolator(DecelerateInterpolator())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        mapBubbleAppearDisappearAnimations.remove(mapBubble)
                        update(mapBubble)
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationRepeat(animation: Animator) {

                    }
                })

        mapBubbleAppearDisappearAnimations[mapBubble] = animator

        view!!.post {
            animator.start()
            update(mapBubble)
        }
    }

    fun update() {
        for (mapBubble in mapBubbles) {
            update(mapBubble)
        }
    }

    fun update(mapBubble: MapBubble) {
        val view = mapBubble.view ?: return

        if (map == null) {
            return
        }

        val point = map!!.projection.toScreenLocation(mapBubble.latLng)
        view.x = (point.x - view.width / 2).toFloat()
        view.y = (point.y - view.height).toFloat()

        mapBubble.view!!.pivotX = (mapBubble.view!!.width / 2).toFloat()
        mapBubble.view!!.pivotY = mapBubble.view!!.height.toFloat()
        if (!mapBubbleAppearDisappearAnimations.containsKey(mapBubble)) {
            mapBubble.view!!.scaleX = zoomScale(mapBubble)
            mapBubble.view!!.scaleY = zoomScale(mapBubble)
        }

        if (this.view!!.height > 0) {
            view.elevation = ((if (mapBubble.isOnTop) 2 else 1) + point.y.toFloat() / this.view!!.height.toFloat()).coerceAtMost(64f)
        }
    }

    fun updateDetails(mapBubble: MapBubble) {
        if (mapBubble.view == null) {
            return
        }

        bubbleView!!.updateView(mapBubble)
        mapBubble.view!!.pivotX = (mapBubble.view!!.width / 2).toFloat()
        mapBubble.view!!.pivotY = mapBubble.view!!.height.toFloat()
        view!!.post { update(mapBubble) }
    }

    fun move(mapBubble: MapBubble, targetLatLng: LatLng) {
        if (mapBubble.inProxy) {
            return
        }

        if (mapBubbleAnimations.containsKey(mapBubble)) {
            val activeAnimator = mapBubbleAnimations[mapBubble]
            if (activeAnimator!!.isRunning) {
                activeAnimator.cancel()
            }
        }

        val sourceLatLng = LatLng(mapBubble.latLng!!.latitude, mapBubble.latLng!!.longitude)

        val animator = ValueAnimator()
        animator.setDuration(1000).setFloatValues(0f, 1f)

        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedFraction

            mapBubble.latLng = LatLng(
                    sourceLatLng.latitude * (1.0 - value.toDouble()) + targetLatLng.latitude * value.toDouble(),
                    sourceLatLng.longitude * (1.0 - value.toDouble()) + targetLatLng.longitude * value.toDouble()
            )
            update(mapBubble)
        }

        animator.start()

        mapBubbleAnimations[mapBubble] = animator
    }

    fun clear() {
        for (mapBubble in mapBubbles) {
            if (mapBubble.isPinned) {
                continue
            }

            remove(mapBubble)
        }
    }

    fun remove(mapBubble: MapBubble) {
        if (!mapBubbles.contains(mapBubble)) {
            return
        }

        view!!.post {
            if (mapBubble.view == null) {
                return@post
            }

            val animator = mapBubble.view!!
                    .animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setInterpolator(AccelerateInterpolator())
                    .setDuration(150)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {

                        }

                        override fun onAnimationEnd(animation: Animator) {
                            mapBubbles.remove(mapBubble)
                            view!!.removeView(mapBubble.view)
                            mapBubble.view = null
                            mapBubbleAppearDisappearAnimations.remove(mapBubble)
                        }

                        override fun onAnimationCancel(animation: Animator) {

                        }

                        override fun onAnimationRepeat(animation: Animator) {

                        }
                    })

            mapBubbleAppearDisappearAnimations[mapBubble] = animator

            animator.start()
        }
    }

    fun remove(callback: (MapBubble) -> Boolean): Boolean {
        val toRemove = HashSet<MapBubble>()
        for (mapBubble in mapBubbles) {
            if (callback.invoke(mapBubble)) {
                toRemove.add(mapBubble)
            }
        }

        if (toRemove.isEmpty()) {
            return false
        }

        for (mapBubble in toRemove) {
            this.remove(mapBubble)
        }

        return true
    }

    private fun zoomScale(mapBubble: MapBubble): Float = when (mapBubble.type) {
        BubbleType.PHYSICAL_GROUP -> (map!!.cameraPosition.zoom / (DEFAULT_ZOOM)).toDouble().pow(10.0).coerceAtLeast(.75).coerceAtMost(2.0).toFloat()
        BubbleType.MENU -> 1f
        else -> 1.0.coerceAtMost((map!!.cameraPosition.zoom / (DEFAULT_ZOOM)).toDouble().pow(10.0)).coerceAtLeast(.5).coerceAtMost(1.0).toFloat()
    }

    interface BubbleView {
        fun createView(view: ViewGroup?, mapBubble: MapBubble): View
        fun updateView(mapBubble: MapBubble)
    }

}
