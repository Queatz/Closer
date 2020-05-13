package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.helpers.Val
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.GroupAction_
import com.queatz.on.On
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class SearchGroupHandler constructor(private val on: On) {

    private var searchQuery = ""
    private var groupsCache: List<Group>? = null

    val groups = BehaviorSubject.create<List<Group>>()
    val createGroupName = BehaviorSubject.createDefault("")

    fun showGroupsForQuery(searchQuery: String) {
        if (this.searchQuery == searchQuery.toLowerCase(Locale.getDefault())) return

        this.searchQuery = searchQuery.toLowerCase(Locale.getDefault())

        createGroupName.onNext(if (searchQuery.isBlank()) "" else searchQuery)

        groupsCache?.let { setGroups(it) }
    }

    fun setGroups(allGroups: List<Group>) {
        this.groupsCache = allGroups

        val groups = mutableListOf<Group>()
        for (group in allGroups) {
            if (group.name != null) {
                if (group.name!!.toLowerCase(Locale.getDefault()).contains(searchQuery) ||
                        on<Val>().of(group.about, "").toLowerCase(Locale.getDefault()).contains(searchQuery) ||
                        groupActionNamesContains(group, searchQuery)) {
                    groups.add(group)
                }
            }
        }

        this.groups.onNext(groups)
    }

    private fun groupActionNamesContains(group: Group, searchQuery: String): Boolean {
        val groupActions = on<StoreHandler>().store.box(GroupAction::class).query()
                .equal(GroupAction_.group, group.id!!).build().find()

        for (groupAction in groupActions) {
            if (groupAction.name!!.toLowerCase(Locale.getDefault()).contains(searchQuery)) {
                return true
            }
        }

        return false
    }
}
