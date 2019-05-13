package closer.vlllage.com.closer.handler.map

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.MapsActivity
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AppShortcutsHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.GroupActionBarButton
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.MyGroupsAdapter
import closer.vlllage.com.closer.handler.helpers.AlertHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import io.objectbox.android.AndroidScheduler
import java.util.*

class MyGroupsLayoutHandler : PoolMember() {
    private var myGroupsLayout: ViewGroup? = null
    private var myGroupsAdapter: MyGroupsAdapter? = null
    private var myGroupsRecyclerView: RecyclerView? = null
    var container: View? = null
        private set

    private var hasSetGroupShortcuts: Boolean = false

    val height: Int
        get() = myGroupsLayout!!.measuredHeight

    fun attach(myGroupsLayout: ViewGroup) {
        this.myGroupsLayout = myGroupsLayout
        myGroupsRecyclerView = myGroupsLayout.findViewById(R.id.myGroupsRecyclerView)
        myGroupsRecyclerView!!.layoutManager = LinearLayoutManager(
                myGroupsRecyclerView!!.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        myGroupsAdapter = MyGroupsAdapter(this)
        `$`(MyGroupsLayoutActionsHandler::class.java).attach(myGroupsAdapter!!)
        myGroupsRecyclerView!!.adapter = myGroupsAdapter
        `$`(DisposableHandler::class.java).add(`$`(StoreHandler::class.java).store.box(Group::class.java).query()
                .notEqual(Group_.isPublic, true)
                .sort(`$`(SortHandler::class.java).sortGroups())
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer { this.setGroups(it) })


        val endActions = ArrayList<GroupActionBarButton>()
        endActions.add(GroupActionBarButton(
                `$`(ResourcesHandler::class.java).resources.getString(R.string.add_new_private_group),
                View.OnClickListener { view ->
                    `$`(AlertHandler::class.java).make().apply {
                        positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.create_group)
                        title = `$`(ResourcesHandler::class.java).resources.getString(R.string.add_new_private_group)
                        layoutResId = R.layout.create_group_modal
                        textViewId = R.id.input
                        onTextViewSubmitCallback = { createGroup(it) }
                        show()
                    }
                }, null,
                R.drawable.clickable_blue_light).also { it.icon = R.drawable.ic_group_add_black_24dp })
        endActions.add(GroupActionBarButton(
                `$`(ResourcesHandler::class.java).resources.getString(R.string.random_suggestion),
                View.OnClickListener { view -> `$`(SuggestionHandler::class.java).shuffle() }, null,
                R.drawable.clickable_green_light

        ).also { it.icon = R.drawable.ic_shuffle_black_24dp })
        endActions.add(GroupActionBarButton(
                `$`(ResourcesHandler::class.java).resources.getString(R.string.settings),
                View.OnClickListener { view -> `$`(MapActivityHandler::class.java).goToScreen(MapsActivity.EXTRA_SCREEN_SETTINGS) }, null,
                R.drawable.clickable_accent

        ).also { it.icon = R.drawable.ic_settings_black_24dp })
        myGroupsAdapter!!.setEndActions(endActions)
    }

    private fun createGroup(name: String?) {
        if (name == null || name.isEmpty()) {
            return
        }

        val group = `$`(StoreHandler::class.java).create(Group::class.java)
        group!!.name = name
        `$`(StoreHandler::class.java).store.box(Group::class.java).put(group)
        `$`(SyncHandler::class.java).sync(group, { groupId ->
            `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(null, groupId)
        })
    }

    private fun setGroups(groups: List<Group>) {
        if (!hasSetGroupShortcuts) {
            `$`(AppShortcutsHandler::class.java).setGroupShortcuts(groups)
            hasSetGroupShortcuts = true
        }
        myGroupsAdapter!!.setGroups(groups)
    }

    fun showBottomPadding(showBottomPadding: Boolean) {
        container!!.setPadding(
                container!!.paddingLeft,
                container!!.paddingTop,
                container!!.paddingRight,
                if (showBottomPadding) `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.feedPeekHeight) else 0
        )
    }

    fun setContainerView(containerView: View) {
        this.container = containerView
    }
}
