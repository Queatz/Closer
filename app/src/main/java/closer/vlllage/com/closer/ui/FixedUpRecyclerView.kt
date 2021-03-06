package closer.vlllage.com.closer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.children
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


open class FixedUpRecyclerView : RecyclerView {

    private var mRequestedLayout = false
    var dispatchTouchEventListener: ((MotionEvent) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        @Suppress("LeakingThis")
        setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
            override fun onChildViewRemoved(parent: View, child: View) {
                if ((layoutManager as? LinearLayoutManager)?.orientation != HORIZONTAL) return
                if (child.height == 0 || parent.height >= child.height) requestLayout()
            }

            override fun onChildViewAdded(parent: View, child: View) {
                if ((layoutManager as? LinearLayoutManager)?.orientation != HORIZONTAL) return
                if (child.height == 0 || parent.height <= child.height) requestLayout()
                if (child.height == 0) child.post { requestLayout() }
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        dispatchTouchEventListener?.invoke(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        // If children were laid out when the view is invisible, they need to relayout
        if (visibility == View.VISIBLE) children.forEach { it.requestLayout() }
    }

    @SuppressLint("WrongCall")
    override fun requestLayout() {
        super.requestLayout()
        // We need to intercept this method because if we don't our children will never update
        // Check https://stackoverflow.com/questions/49371866/recyclerview-wont-update-child-until-i-scroll
        if (!mRequestedLayout) {
            mRequestedLayout = true
            doOnNextLayout {
                mRequestedLayout = false
                layout(left, top, right, bottom)
                onLayout(false, left, top, right, bottom)
            }
        }
    }
}