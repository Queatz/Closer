package closer.vlllage.com.closer.handler.helpers

import android.view.View

import com.queatz.on.On

class ViewAttributeHandler constructor(private val on: On) {
    fun linkPadding(targetView: View, sourceView: View) {
        sourceView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            targetView.setPadding(sourceView.paddingLeft, sourceView.paddingTop,
                    sourceView.paddingRight, sourceView.paddingBottom)
        }
    }
}
