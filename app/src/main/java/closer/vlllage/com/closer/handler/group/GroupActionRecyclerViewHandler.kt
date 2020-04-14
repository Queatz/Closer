package closer.vlllage.com.closer.handler.group

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.store.models.GroupAction
import com.queatz.on.On

class GroupActionRecyclerViewHandler constructor(private val on: On) {

    var adapter: GroupActionAdapter? = null
        private set
    var recyclerView: RecyclerView? = null
        private set

    fun attach(actionRecyclerView: RecyclerView, layout: GroupActionDisplay.Layout) {
        recyclerView = actionRecyclerView
        actionRecyclerView.layoutManager = LinearLayoutManager(
                on<ActivityHandler>().activity,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        adapter = GroupActionAdapter(on, layout)

        actionRecyclerView.adapter = adapter
    }
}
