package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.pool.PoolActivityFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import io.objectbox.android.AndroidScheduler
import kotlinx.android.synthetic.main.fragment_group_events.*

class GroupEventsFragment : PoolActivityFragment() {

    private lateinit var disposableGroup: DisposableGroup
    private lateinit var groupDisposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_group_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        groupDisposableGroup = disposableGroup.group()

        val groupMessagesAdapter = GroupMessagesAdapter(on)
        on<GroupMessageHelper>().global = true
        on<GroupMessageHelper>().onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
        on<GroupMessageHelper>().onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(view, event) }
        on<GroupMessageHelper>().onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(view, group1.id) }

        messagesRecyclerView.layoutManager = LinearLayoutManager(messagesRecyclerView.context)
        messagesRecyclerView.adapter = groupMessagesAdapter

        on<GroupHandler> {
            onGroupChanged(disposableGroup) { group ->
                groupDisposableGroup.clear()

                hostEvent.setOnClickListener {
                    on<HostEventHelper>().hostEvent(group)
                }

                val queryBuilder = on<StoreHandler>().store.box(GroupMessage::class).query()
                groupDisposableGroup.add(queryBuilder
                        .sort(on<SortHandler>().sortGroupMessages())
                        .equal(GroupMessage_.to, group.id!!)
                        .contains(GroupMessage_.attachment, "\"event\"")
                        .build()
                        .subscribe()
                        .on(AndroidScheduler.mainThread())
                        .observer { groupMessages ->
                            emptyText.visible = groupMessages.isEmpty()
                            hostEvent.visible = groupMessages.isEmpty()

                            groupMessagesAdapter.setGroupMessages(groupMessages)
                        })
            }
        }

        on<DisposableHandler>().add(on<LightDarkHandler>().onLightChanged.subscribe {
            emptyText.setTextColor(it.text)
            hostEvent.setTextColor(it.text)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }

}
