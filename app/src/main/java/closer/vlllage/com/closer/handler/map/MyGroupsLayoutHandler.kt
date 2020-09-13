package closer.vlllage.com.closer.handler.map

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.MyGroupsAdapter
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.queatz.on.On

class MyGroupsLayoutHandler constructor(private val on: On) {
    private var myGroupsLayout: ViewGroup? = null
    private var myGroupsAdapter: MyGroupsAdapter? = null
    private var myGroupsRecyclerView: RecyclerView? = null
    var container: View? = null
        private set

    val height: Int
        get() = myGroupsLayout!!.measuredHeight

    fun attach(myGroupsLayout: ViewGroup) {
        this.myGroupsLayout = myGroupsLayout
        myGroupsRecyclerView = myGroupsLayout.findViewById(R.id.myGroupsRecyclerView)
        myGroupsRecyclerView!!.layoutManager = LinearLayoutManager(
                myGroupsRecyclerView!!.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        myGroupsAdapter = MyGroupsAdapter(on)
        on<MyGroupsLayoutActionsHandler>().attach(myGroupsAdapter!!)
        myGroupsRecyclerView!!.adapter = myGroupsAdapter
    }

    fun showBottomPadding(showBottomPadding: Boolean) {
        container!!.setPadding(
                container!!.paddingStart,
                container!!.paddingTop,
                container!!.paddingEnd,
                if (showBottomPadding) on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.feedPeekHeight) else 0
        )
    }

    fun setContainerView(containerView: View) {
        this.container = containerView
    }
}
