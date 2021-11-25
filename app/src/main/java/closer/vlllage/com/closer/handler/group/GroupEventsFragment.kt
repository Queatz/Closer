package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.databinding.FragmentGroupEventsBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.pool.PoolActivityFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import io.objectbox.android.AndroidScheduler
import io.objectbox.query.QueryBuilder

class GroupEventsFragment : PoolActivityFragment() {

    private lateinit var binding: FragmentGroupEventsBinding
    private lateinit var disposableGroup: DisposableGroup
    private lateinit var groupDisposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentGroupEventsBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        groupDisposableGroup = disposableGroup.group()

        val groupMessagesAdapter = GroupMessagesAdapter(on)
        on<GroupMessageHelper>().global = true
        on<GroupMessageHelper>().onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
        on<GroupMessageHelper>().onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(view, event) }
        on<GroupMessageHelper>().onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(view, group1.id) }

        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(binding.messagesRecyclerView.context)
        binding.messagesRecyclerView.adapter = groupMessagesAdapter

        on<GroupHandler> {
            onGroupChanged(disposableGroup) { group ->
                groupDisposableGroup.clear()

                binding.hostEvent.setOnClickListener {
                    on<HostEventHelper>().hostEvent(group)
                }

                val queryBuilder = on<StoreHandler>().store.box(GroupMessage::class).query()
                groupDisposableGroup.add(queryBuilder
                        .sort(on<SortHandler>().sortGroupMessages())
                        .equal(GroupMessage_.to, group.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        .contains(GroupMessage_.attachment, "\"event\"", QueryBuilder.StringOrder.CASE_SENSITIVE)
                        .build()
                        .subscribe()
                        .on(AndroidScheduler.mainThread())
                        .observer { groupMessages ->
                            binding.emptyText.visible = groupMessages.isEmpty()
                            binding.hostEvent.visible = groupMessages.isEmpty()

                            groupMessagesAdapter.setGroupMessages(groupMessages)
                        })
            }
        }

        on<DisposableHandler>().add(on<LightDarkHandler>().onLightChanged.subscribe {
            binding.emptyText.setTextColor(it.text)
            binding.hostEvent.setTextColor(it.text)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }

}
