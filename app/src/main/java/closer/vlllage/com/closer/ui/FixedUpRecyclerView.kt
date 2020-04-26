package closer.vlllage.com.closer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


open class FixedUpRecyclerView : RecyclerView {

    private var mRequestedLayout = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init { viewTreeObserver.addOnGlobalLayoutListener { requestLayout() } }

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