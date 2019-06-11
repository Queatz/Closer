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
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.google.android.gms.maps.model.CameraPosition
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.create_group_modal.view.*

class PublicGroupFeedItemHandler constructor(private val on: On) {

    private lateinit var searchGroups: EditText

    fun attach(itemView: View) {
        val groupsRecyclerView = itemView.findViewById<RecyclerView>(R.id.publicGroupsRecyclerView)
        searchGroups = itemView.findViewById(R.id.searchGroups)

        val searchGroupsAdapter = SearchGroupsAdapter(on, true, { group, view -> openGroup(group.id, view) }, { groupName: String -> createGroup(groupName) })
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_card_item)

        groupsRecyclerView.adapter = searchGroupsAdapter
        groupsRecyclerView.layoutManager = LinearLayoutManager(
                groupsRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        searchGroups.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                on<SearchGroupHandler>().showGroupsForQuery(searchGroupsAdapter, searchGroups.text.toString())
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        on<SearchGroupHandler>().showGroupsForQuery(searchGroupsAdapter, searchGroups.text.toString())

        val distance = .12f

        val cameraPositionCallback = { cameraPosition: CameraPosition ->
            val queryBuilder = on<StoreHandler>().store.box(Group::class).query()
                    .between(Group_.latitude, cameraPosition.target.latitude - distance, cameraPosition.target.latitude + distance)
                    .and()
                    .between(Group_.longitude, cameraPosition.target.longitude - distance, cameraPosition.target.longitude + distance)
                    .or()
                    .equal(Group_.isPublic, false)

            on<DisposableHandler>().add(queryBuilder
                    .sort(on<SortHandler>().sortGroups(false))
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .single()
                    .observer { groups ->
                        on<SearchGroupHandler>().setGroups(groups)
                        on<TimerHandler>().post(Runnable { groupsRecyclerView.scrollBy(0, 0) })
                    })
        }

        on<DisposableHandler>().add(on<MapHandler>().onMapIdleObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cameraPositionCallback))
    }

    private fun createGroup(groupName: String?) {
        if (groupName.isNullOrBlank()) {
            on<AlertHandler>().make().apply {
                title = on<ResourcesHandler>().resources.getString(R.string.create_public_group)
                layoutResId = R.layout.create_group_modal
                textViewId = R.id.input
                onTextViewSubmitCallback = {
                    addGroupDescription(it.trim())
                }
                onAfterViewCreated = { alertConfig, view ->
                    alertConfig.alertResult = view.input
                }
                buttonClickCallback = {
                    (it as EditText).text.isNotBlank()
                }
                positiveButton = on<ResourcesHandler>().resources.getString(R.string.continue_text)
                show()
            }
        } else {
            addGroupDescription(groupName)
        }
    }

    private fun addGroupDescription(groupName: String) {
        searchGroups.setText("")

        on<LocationHandler>().getCurrentLocation({ location ->
            on<AlertHandler>().make().apply {
                title = on<ResourcesHandler>().resources.getString(R.string.group_as_public, groupName)
                layoutResId = R.layout.create_public_group_modal
                textViewId = R.id.input
                onTextViewSubmitCallback = { about ->
                    val group = on<StoreHandler>().create(Group::class.java)
                    group!!.name = groupName
                    group.about = about
                    group.isPublic = true
                    group.latitude = location.latitude
                    group.longitude = location.longitude
                    on<StoreHandler>().store.box(Group::class).put(group)
                    on<SyncHandler>().sync(group, { groupId ->
                        openGroup(groupId, null)
                    })
                }
                positiveButton = on<ResourcesHandler>().resources.getString(R.string.create_public_group)
                show()
            }

        }, { on<DefaultAlerts>().thatDidntWork(on<ResourcesHandler>().resources.getString(R.string.location_is_needed)) })
    }

    fun openGroup(groupId: String?, view: View?) {
        on<GroupActivityTransitionHandler>().showGroupMessages(view, groupId)
    }

}
