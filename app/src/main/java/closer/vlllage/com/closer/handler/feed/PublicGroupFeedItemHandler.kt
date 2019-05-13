package closer.vlllage.com.closer.handler.feed

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.LocationHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.SearchGroupHandler
import closer.vlllage.com.closer.handler.group.SearchGroupsAdapter
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.google.android.gms.maps.model.CameraPosition
import io.objectbox.android.AndroidScheduler
import io.reactivex.android.schedulers.AndroidSchedulers

class PublicGroupFeedItemHandler : PoolMember() {

    private var searchGroups: EditText? = null

    fun attach(itemView: View) {
        val groupsRecyclerView = itemView.findViewById<RecyclerView>(R.id.publicGroupsRecyclerView)
        searchGroups = itemView.findViewById(R.id.searchGroups)

        val searchGroupsAdapter = SearchGroupsAdapter(`$`(PoolMember::class.java), { group, view -> openGroup(group.id, view) }, { groupName: String -> this.createGroup(groupName) })
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_card_item)

        groupsRecyclerView.adapter = searchGroupsAdapter
        groupsRecyclerView.layoutManager = LinearLayoutManager(
                groupsRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        searchGroups!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                `$`(SearchGroupHandler::class.java).showGroupsForQuery(searchGroupsAdapter, searchGroups!!.text.toString())
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        `$`(SearchGroupHandler::class.java).showGroupsForQuery(searchGroupsAdapter, searchGroups!!.text.toString())

        val distance = .12f

        val cameraPositionCallback = { cameraPosition: CameraPosition ->
            val queryBuilder = `$`(StoreHandler::class.java).store.box(Group::class.java).query()
                    .between(Group_.latitude, cameraPosition.target.latitude - distance, cameraPosition.target.latitude + distance)
                    .and()
                    .between(Group_.longitude, cameraPosition.target.longitude - distance, cameraPosition.target.longitude + distance)
                    .or()
                    .equal(Group_.isPublic, false)

            `$`(DisposableHandler::class.java).add(queryBuilder
                    .sort(`$`(SortHandler::class.java).sortGroups(false))
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .single()
                    .observer { groups ->
                        `$`(SearchGroupHandler::class.java).setGroups(groups)
                        `$`(TimerHandler::class.java).post(Runnable { groupsRecyclerView.scrollBy(0, 0) })
                    })
        }

        `$`(DisposableHandler::class.java).add(`$`(MapHandler::class.java).onMapIdleObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cameraPositionCallback))
    }

    private fun createGroup(groupName: String?) {
        if (groupName == null || groupName.isEmpty()) {
            return
        }

        searchGroups!!.setText("")

        `$`(LocationHandler::class.java).getCurrentLocation({ location ->
            `$`(AlertHandler::class.java).make().apply {
                title = `$`(ResourcesHandler::class.java).resources.getString(R.string.group_as_public, groupName)
                layoutResId = R.layout.create_public_group_modal
                textViewId = R.id.input
                onTextViewSubmitCallback = { about ->
                    val group = `$`(StoreHandler::class.java).create(Group::class.java)
                    group!!.name = groupName
                    group.about = about
                    group.isPublic = true
                    group.latitude = location.latitude
                    group.longitude = location.longitude
                    `$`(StoreHandler::class.java).store.box(Group::class.java).put(group)
                    `$`(SyncHandler::class.java).sync(group, object : SyncHandler.OnSyncResult {
                        override fun onSync(groupId: String?) {
                            openGroup(groupId, null)
                        }
                    })
                }
                positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.create_public_group)
                show()
            }

        }, { `$`(DefaultAlerts::class.java).thatDidntWork(`$`(ResourcesHandler::class.java).resources.getString(R.string.location_is_needed)) })
    }

    fun openGroup(groupId: String?, view: View?) {
        `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(view, groupId)
    }

}
