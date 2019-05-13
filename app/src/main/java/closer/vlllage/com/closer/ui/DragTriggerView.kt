package closer.vlllage.com.closer.ui

import android.view.MotionEvent
import android.view.View

class DragTriggerView(view: View, onDragEventListener: OnDragEventListener) {

    init {

        view.isClickable = true
        view.setOnTouchListener(object : View.OnTouchListener {

            private val trackedX = TimedValue<Float>(75)
            private val trackedY = TimedValue<Float>(75)

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        trackedX.report(motionEvent.rawX)
                        trackedY.report(motionEvent.rawY)
                        onDragEventListener.onDragStart(trackedX, trackedY)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        trackedX.report(motionEvent.rawX)
                        trackedY.report(motionEvent.rawY)
                        onDragEventListener.onDragUpdate(trackedX, trackedY)
                    }
                    MotionEvent.ACTION_UP -> {
                        trackedX.report(motionEvent.rawX)
                        trackedY.report(motionEvent.rawY)
                        onDragEventListener.onDragRelease(trackedX, trackedY)
                    }
                }

                return true
            }
        })
    }

    interface OnDragEventListener {
        fun onDragStart(x: TimedValue<Float>, y: TimedValue<Float>)
        fun onDragRelease(x: TimedValue<Float>, y: TimedValue<Float>)
        fun onDragUpdate(x: TimedValue<Float>, y: TimedValue<Float>)
    }
}
