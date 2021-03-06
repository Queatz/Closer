package closer.vlllage.com.closer.handler.group

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler

class PinnedMessagesHandler constructor(private val on: On) {

    private lateinit var pinnedMessagesRecyclerView: RecyclerView
    private lateinit var groupMessagesAdapter: GroupMessagesAdapter
    private var disposableGroup = on<DisposableHandler>().group()

    fun attach(pinnedMessagesRecyclerView: RecyclerView) {
        this.pinnedMessagesRecyclerView = pinnedMessagesRecyclerView

        pinnedMessagesRecyclerView.layoutManager = LinearLayoutManager(
                on<ActivityHandler>().activity,
                RecyclerView.VERTICAL,
                true
        )

        groupMessagesAdapter = GroupMessagesAdapter(On(on).apply {
            use<GroupMessageHelper> {
                pinned = true
                onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
                onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(null, event) }
                onGroupClickListener =  { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(null, group1.id) }
            }
        })

        pinnedMessagesRecyclerView.adapter = groupMessagesAdapter
    }

    fun show(group: Group) {
        disposableGroup.clear()

        on<RefreshHandler>().refreshPins(group.id!!)

        on<StoreHandler>().store.box(Pin::class).query()
                .equal(Pin_.to, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { pins ->
                    if (pins.isEmpty()) {
                        setGroupMessages(listOf())
                        return@observer
                    }

                    val ids = mutableListOf<String>()

                    for (pin in pins) {
                        ids.add(pin.from!!)
                    }

                    on<StoreHandler>().store.box(GroupMessage::class).query()
                            .`in`(GroupMessage_.id, ids.toTypedArray())
                            .build()
                            .subscribe()
                            .on(AndroidScheduler.mainThread())
                            .observer { this.setGroupMessages(it) }
                            .also { disposableGroup.add(it) }
                }
                .also { disposableGroup.add(it) }
    }

    private fun setGroupMessages(pinnedMessages: List<GroupMessage>) {
        pinnedMessagesRecyclerView.visibility = if (pinnedMessages.isEmpty()) View.GONE else View.VISIBLE
        groupMessagesAdapter.setGroupMessages(pinnedMessages)
    }
}
