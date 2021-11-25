package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.FragmentPhoneMessagesBinding
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.DisposableGroup
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.pool.PoolActivityFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import io.objectbox.android.AndroidScheduler
import io.objectbox.query.QueryBuilder

class PhoneMessagesFragment : PoolActivityFragment() {

    private lateinit var binding: FragmentPhoneMessagesBinding
    private lateinit var disposableGroup: DisposableGroup
    private lateinit var phoneDisposableGroup: DisposableGroup
    private lateinit var groupMessagesAdapter: GroupMessagesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentPhoneMessagesBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        phoneDisposableGroup = disposableGroup.group()

        groupMessagesAdapter = GroupMessagesAdapter(on)
        on<GroupMessageHelper>().global = true
        on<GroupMessageHelper>().onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
        on<GroupMessageHelper>().onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(view, event) }
        on<GroupMessageHelper>().onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(view, group1.id) }

        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(binding.messagesRecyclerView.context)
        binding.messagesRecyclerView.adapter = groupMessagesAdapter

        on<GroupHandler> {
            onPhoneChanged(disposableGroup) { phone ->
                phoneDisposableGroup.clear()

                on<RefreshHandler>().refreshGroupMessagesForPhone(phone.id!!)

                on<StoreHandler>().store.box(GroupMessage::class).query()
                        .equal(GroupMessage_.from, phone.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        .sort(on<SortHandler>().sortGroupMessages())
                        .build()
                        .subscribe()
                        .on(AndroidScheduler.mainThread())
                        .observer { groupMessagesAdapter.setGroupMessages(it) }
                        .also { phoneDisposableGroup.add(it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}
