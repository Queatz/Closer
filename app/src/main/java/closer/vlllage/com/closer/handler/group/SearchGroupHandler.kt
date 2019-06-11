package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.helpers.Val
import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.GroupAction_
import java.util.*

class SearchGroupHandler constructor(private val on: On) {

    private var searchQuery = ""
    private var searchGroupsAdapter: SearchGroupsAdapter? = null
    private var groupsCache: List<Group>? = null
    private var hideCreateGroupOption: Boolean = false

    fun showGroupsForQuery(searchGroupsAdapter: SearchGroupsAdapter, searchQuery: String) {
        this.searchQuery = searchQuery.toLowerCase()
        this.searchGroupsAdapter = searchGroupsAdapter

        if (!hideCreateGroupOption) {
            searchGroupsAdapter.setCreatePublicGroupName(if (searchQuery.isBlank()) null else searchQuery)
        }

        if (this.groupsCache != null) {
            setGroups(this.groupsCache!!)
        }
    }

    fun setGroups(allGroups: List<Group>) {
        this.groupsCache = allGroups

        val groups = ArrayList<Group>()
        for (group in allGroups) {
            if (group.name != null) {
                if (group.name!!.toLowerCase().contains(searchQuery) ||
                        on<Val>().of(group.about, "").toLowerCase().contains(searchQuery) ||
                        groupActionNamesContains(group, searchQuery)) {
                    groups.add(group)
                }
            }
        }

        searchGroupsAdapter!!.setGroups(groups)
    }

    private fun groupActionNamesContains(group: Group, searchQuery: String): Boolean {
        val groupActions = on<StoreHandler>().store.box(GroupAction::class).query()
                .equal(GroupAction_.group, group.id!!).build().find()

        for (groupAction in groupActions) {
            if (groupAction.name!!.toLowerCase().contains(searchQuery)) {
                return true
            }
        }

        return false
    }

    fun hideCreateGroupOption() {
        this.hideCreateGroupOption = true
    }
}
