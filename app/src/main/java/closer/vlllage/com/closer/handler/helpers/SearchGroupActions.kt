package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.GroupAction_
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.kotlin.or
import io.objectbox.query.QueryBuilder

class SearchGroupActions constructor(private val on: On) {
    fun observe(groups: List<Group>? = null, queryString: String? = null, callback: (List<GroupAction>) -> Unit) = on<StoreHandler>().store.box(GroupAction::class).query(
            GroupAction_.group.notNull().let { query ->
                (groups?.let {
                    query.and(GroupAction_.group.oneOf(groups
                            .mapNotNull { it.id }
                            .toTypedArray()))
                } ?: query).let { query ->
                    queryString?.takeIf { it.isNotBlank() }?.let {
                        query.and(GroupAction_.about.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE) or
                                GroupAction_.name.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE) or
                                GroupAction_.flow.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE))
                    } ?: query
                }
            }
    )
            .sort(on<SortHandler>().sortGroupActions())
            .build()
            .subscribe()
            .on(AndroidScheduler.mainThread())
            .observer { callback(it) }!!

}
