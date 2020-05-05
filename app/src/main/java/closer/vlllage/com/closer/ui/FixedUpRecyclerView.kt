package closer.vlllage.com.closer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


open class FixedUpRecyclerView : RecyclerView {

    private var mRequestedLayout = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        @Suppress("LeakingThis") setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
            override fun onChildViewRemoved(parent: View, child: View) {
                if ((layoutManager as? LinearLayoutManager)?.orientation != HORIZONTAL) return
                if (child.height == 0 || parent.height >= child.height) post { requestLayout() }
            }

            override fun onChildViewAdded(parent: View, child: View) {
                if ((layoutManager as? LinearLayoutManager)?.orientation != HORIZONTAL) return
                if (child.height == 0 || parent.height <= child.height) post { requestLayout() }
                if (child.height == 0) child.post { child.requestLayout() }
            }
        })
    }

    @SuppressLint("WrongCall")
    override fun requestLayout() {
        super.requestLayout()
        // We need to intercept this method because if we don't our children will never update
        // Check https://stackoverflow.com/questions/49371866/recyclerview-wont-update-child-until-i-scroll
        if (!mRequestedLayout) {
            mRequestedLayout = true
            post {
                mRequestedLayout = false
                layout(left, top, right, bottom)
                onLayout(false, left, top, right, bottom)
            }
        }
    }
}