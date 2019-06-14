package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolActivityFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import closer.vlllage.com.closer.ui.CircularRevealActivity
import kotlinx.android.synthetic.main.fragment_share_group.*

class ShareGroupFragment : PoolActivityFragment() {

    private lateinit var disposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_share_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()

        val queryBuilder = on<StoreHandler>().store.box(Group::class).query()
        val groups = queryBuilder.sort(on<SortHandler>().sortGroups()).notEqual(Group_.physical, true).build().find()

        val searchGroupsAdapter = SearchGroupsAdapter(on, false, { group, view ->
            val success = on<GroupMessageAttachmentHandler>().shareEvent(on<GroupHandler>().event!!, group)

            if (success) {
                (on<ActivityHandler>().activity as CircularRevealActivity)
                        .finish { on<GroupActivityTransitionHandler>().showGroupMessages(view, group.id) }
            } else {
                on<DefaultAlerts>().thatDidntWork()
            }
        }, null)

        searchGroupsAdapter.setGroups(groups)
        searchGroupsAdapter.setActionText(on<ResourcesHandler>().resources.getString(R.string.share))
        searchGroupsAdapter.setIsSmall(true)

        shareWithRecyclerView.layoutManager = LinearLayoutManager(shareWithRecyclerView.context)
        shareWithRecyclerView.adapter = searchGroupsAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}