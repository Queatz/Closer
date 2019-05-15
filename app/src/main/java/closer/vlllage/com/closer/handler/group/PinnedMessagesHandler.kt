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
import io.objectbox.reactive.DataSubscription
import java.util.*

class PinnedMessagesHandler constructor(private val on: On) {

    private lateinit var pinnedMessagesRecyclerView: RecyclerView
    private lateinit var groupMessagesAdapter: GroupMessagesAdapter
    private var groupMessagesSubscription: DataSubscription? = null
    private var groupMessagesActualSubscription: DataSubscription? = null

    fun attach(pinnedMessagesRecyclerView: RecyclerView) {
        this.pinnedMessagesRecyclerView = pinnedMessagesRecyclerView

        pinnedMessagesRecyclerView.layoutManager = LinearLayoutManager(
                on<ActivityHandler>().activity,
                RecyclerView.VERTICAL,
                true
        )

        groupMessagesAdapter = GroupMessagesAdapter(on)
        groupMessagesAdapter.setPinned(true)
        pinnedMessagesRecyclerView.adapter = groupMessagesAdapter
        groupMessagesAdapter.onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
        groupMessagesAdapter.onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(null, event) }
        groupMessagesAdapter.onGroupClickListener =  { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(null, group1.id) }
    }

    fun show(group: Group) {
        if (groupMessagesSubscription != null) {
            on<DisposableHandler>().dispose(groupMessagesSubscription!!)
        }

        if (groupMessagesActualSubscription != null) {
            on<DisposableHandler>().dispose(groupMessagesSubscription!!)
        }

        on<RefreshHandler>().refreshPins(group.id!!)

        groupMessagesSubscription = on<StoreHandler>().store.box(Pin::class.java).query()
                .equal(Pin_.to, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { pins ->
                    if (pins.isEmpty()) {
                        setGroupMessages(ArrayList())
                        return@observer
                    }

                    val ids = ArrayList<String>()

                    for (pin in pins) {
                        ids.add(pin.from!!)
                    }

                    groupMessagesActualSubscription = on<StoreHandler>().store.box(GroupMessage::class.java).query()
                            .`in`(GroupMessage_.id, ids.toTypedArray())
                            .build()
                            .subscribe()
                            .single()
                            .on(AndroidScheduler.mainThread())
                            .observer { this.setGroupMessages(it) }
                    on<DisposableHandler>().add(groupMessagesActualSubscription!!)
                }

        on<DisposableHandler>().add(groupMessagesSubscription!!)
    }

    private fun setGroupMessages(pinnedMessages: List<GroupMessage>) {
        pinnedMessagesRecyclerView.visibility = if (pinnedMessages.isEmpty()) View.GONE else View.VISIBLE
        groupMessagesAdapter.setGroupMessages(pinnedMessages)
    }
}
