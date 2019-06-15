package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
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
import kotlinx.android.synthetic.main.fragment_phone_messages.*

class PhoneMessagesFragment : PoolActivityFragment() {

    private lateinit var disposableGroup: DisposableGroup
    private lateinit var phoneDisposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_phone_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        phoneDisposableGroup = disposableGroup.group()

        val groupMessagesAdapter = GroupMessagesAdapter(on)
        groupMessagesAdapter.global = true
        groupMessagesAdapter.onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
        groupMessagesAdapter.onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(view, event) }
        groupMessagesAdapter.onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(view, group1.id) }

        messagesRecyclerView.layoutManager = LinearLayoutManager(messagesRecyclerView.context)
        messagesRecyclerView.adapter = groupMessagesAdapter

        on<GroupHandler> {
            onPhoneChanged(disposableGroup) { phone ->
                phoneDisposableGroup.clear()

                on<RefreshHandler>().refreshGroupMessagesForPhone(phone.id!!)

                val queryBuilder = on<StoreHandler>().store.box(GroupMessage::class).query()
                phoneDisposableGroup.add(queryBuilder
                        .sort(on<SortHandler>().sortGroupMessages())
                        .equal(GroupMessage_.from, phone.id!!)
                        .build()
                        .subscribe()
                        .on(AndroidScheduler.mainThread())
                        .observer { groupMessages ->
                            groupMessagesAdapter.setGroupMessages(groupMessages)
                        })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}