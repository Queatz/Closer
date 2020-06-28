package closer.vlllage.com.closer.handler.group

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.store.models.GroupAction
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.reactivex.android.schedulers.AndroidSchedulers

class GroupActionRecyclerViewHandler constructor(private val on: On) {

    lateinit var adapter: GroupActionAdapter
        private set
    var recyclerView: RecyclerView? = null
        private set

    private lateinit var layoutManager: GridLayoutManager

    fun attach(actionRecyclerView: RecyclerView, layout: GroupActionDisplay.Layout) {
        recyclerView = actionRecyclerView
        actionRecyclerView.layoutManager = GridLayoutManager(
                recyclerView!!.context,
                1,
                RecyclerView.VERTICAL,
                false
        ).also {
            layoutManager = it
        }

        adapter = GroupActionAdapter(on, layout)

        adapter.onItemsChanged.map { it.size }.distinctUntilChanged().observeOn(AndroidSchedulers.mainThread()).subscribe {
            layoutManager.spanCount = when {
                it == 1 -> 1
                else -> it.coerceIn(2, 3)
            }
            adapter.scale = if (layoutManager.spanCount == 1) 1.5f else if (layoutManager.spanCount > 2) .75f else 1f
        }.also {
            on<DisposableHandler>().add(it)
        }

        actionRecyclerView.adapter = adapter
    }

    fun setLayout(layout: GroupActionDisplay.Layout) {
        adapter.layout = layout
    }
}
