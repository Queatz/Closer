package closer.vlllage.com.closer.handler.helpers

import android.view.View

import closer.vlllage.com.closer.pool.PoolMember

class ViewAttributeHandler : PoolMember() {
    fun linkPadding(targetView: View, sourceView: View) {
        sourceView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            targetView.setPadding(sourceView.paddingLeft, sourceView.paddingTop,
                    sourceView.paddingRight, sourceView.paddingBottom)
        }
    }
}
