package closer.vlllage.com.closer.handler.map

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.handler.feed.GroupPreviewAdapter
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.KeyboardVisibilityHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.reactivex.android.schedulers.AndroidSchedulers

class FeedHandler constructor(private val on: On) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var groupPreviewAdapter: GroupPreviewAdapter

    fun attach(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        layoutManager = LinearLayoutManager(
                recyclerView.context,
                RecyclerView.VERTICAL,
                false
        )
        recyclerView.layoutManager = layoutManager

        groupPreviewAdapter = GroupPreviewAdapter(on)
        recyclerView.adapter = groupPreviewAdapter

        val distance = .12f

        on<DisposableHandler>().add(on<MapHandler>().onMapIdleObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { cameraPosition ->
                    val groupPreviewQueryBuilder = on<StoreHandler>().store.box(Group::class).query()
                            .between(Group_.latitude, cameraPosition.target.latitude - distance, cameraPosition.target.latitude + distance)
                            .and()
                            .between(Group_.longitude, cameraPosition.target.longitude - distance, cameraPosition.target.longitude + distance)
                            .or()
                            .equal(Group_.isPublic, false)
                    on<DisposableHandler>().add(groupPreviewQueryBuilder
                            .sort(on<SortHandler>().sortGroups(false))
                            .build()
                            .subscribe()
                            .single()
                            .on(AndroidScheduler.mainThread())
                            .observer { setGroups(it) })
                })

        on<DisposableHandler>().add(on<KeyboardVisibilityHandler>().isKeyboardVisible.subscribe { visible ->
            if (visible) {
                recyclerView.setPadding(0, 0, 0, on<KeyboardVisibilityHandler>().lastKeyboardHeight)
            } else {
                recyclerView.setPadding(0, 0, 0, 0)
            }
        })
    }

    private fun setGroups(groups: List<Group>) {
        groupPreviewAdapter.groups = groups.toMutableList()
    }

    fun hide() {
        if (layoutManager.findFirstVisibleItemPosition() > 2) {
            recyclerView.scrollToPosition(2)
        }

        recyclerView.smoothScrollToPosition(0)
    }
}
