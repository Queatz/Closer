package closer.vlllage.com.closer.handler.group

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import java.util.*

class PinnedMessagesHandler : PoolMember() {

    private var pinnedMessagesRecyclerView: RecyclerView? = null
    private var groupMessagesAdapter: GroupMessagesAdapter? = null
    private var groupMessagesSubscription: DataSubscription? = null
    private var groupMessagesActualSubscription: DataSubscription? = null

    fun attach(pinnedMessagesRecyclerView: RecyclerView) {
        this.pinnedMessagesRecyclerView = pinnedMessagesRecyclerView

        pinnedMessagesRecyclerView.layoutManager = LinearLayoutManager(
                `$`(ActivityHandler::class.java).activity,
                LinearLayoutManager.VERTICAL,
                true
        )

        groupMessagesAdapter = GroupMessagesAdapter(this)
        groupMessagesAdapter!!.setPinned(true)
        pinnedMessagesRecyclerView.adapter = groupMessagesAdapter
        groupMessagesAdapter!!.onSuggestionClickListener = { suggestion -> `$`(MapActivityHandler::class.java).showSuggestionOnMap(suggestion) }
        groupMessagesAdapter!!.onEventClickListener = { event -> `$`(GroupActivityTransitionHandler::class.java).showGroupForEvent(null, event) }
        groupMessagesAdapter!!.onGroupClickListener =  { group1 -> `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(null, group1.id) }
    }

    fun show(group: Group) {
        if (groupMessagesSubscription != null) {
            `$`(DisposableHandler::class.java).dispose(groupMessagesSubscription!!)
        }

        if (groupMessagesActualSubscription != null) {
            `$`(DisposableHandler::class.java).dispose(groupMessagesSubscription!!)
        }

        `$`(RefreshHandler::class.java).refreshPins(group.id!!)

        groupMessagesSubscription = `$`(StoreHandler::class.java).store.box(Pin::class.java).query()
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

                    groupMessagesActualSubscription = `$`(StoreHandler::class.java).store.box(GroupMessage::class.java).query()
                            .`in`(GroupMessage_.id, ids.toTypedArray())
                            .build()
                            .subscribe()
                            .single()
                            .on(AndroidScheduler.mainThread())
                            .observer { this.setGroupMessages(it) }
                    `$`(DisposableHandler::class.java).add(groupMessagesActualSubscription!!)
                }

        `$`(DisposableHandler::class.java).add(groupMessagesSubscription!!)
    }

    private fun setGroupMessages(pinnedMessages: List<GroupMessage>) {
        pinnedMessagesRecyclerView!!.visibility = if (pinnedMessages.isEmpty()) View.GONE else View.VISIBLE
        groupMessagesAdapter!!.setGroupMessages(pinnedMessages)
    }
}
