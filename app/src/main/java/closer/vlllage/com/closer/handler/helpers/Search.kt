package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.kotlin.or
import io.objectbox.query.QueryBuilder
import io.objectbox.reactive.DataSubscription
import java.util.*

class Search constructor(private val on: On) {
    fun physicalGroups(latLng: LatLng, queryString: String? = null, privateOnly: Boolean = false, single: Boolean = false, callback: (List<Group>) -> Unit): DataSubscription {
        val distance = on<HowFar>().about7Miles

        return on<StoreHandler>().store.box(Group::class).query(Group_.physical.equal(true).and(
                Group_.latitude.between(latLng.latitude - distance, latLng.latitude + distance).and(Group_.longitude.between(latLng.longitude - distance, latLng.longitude + distance))
        ).let { query ->
            if (privateOnly) query.and(Group_.isPublic.equal(false)) else query
        }.let { query ->
            queryString?.takeIf { it.isNotBlank() }?.let {
                query.and(Group_.about.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE) or
                        Group_.name.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE))
            } ?: query } )
                .sort(on<SortHandler>().sortPhysicalGroups(latLng))
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .let { if (single) it.single() else it }
                .observer {
                    callback(it)
                }
    }

    fun phones(latLng: LatLng, queryString: String? = null, single: Boolean = false, callback: (List<Phone>) -> Unit): DataSubscription {
        val distance = on<HowFar>().about7Miles

        return on<StoreHandler>().store.box(Phone::class).query(
                Phone_.latitude.between(latLng.latitude - distance, latLng.latitude + distance).and(
                Phone_.longitude.between(latLng.longitude - distance, latLng.longitude + distance)).let { query ->
                    queryString?.takeIf { it.isNotBlank() }?.let {
                        query.and(Phone_.introduction.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE) or
                                Phone_.status.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE) or
                                Phone_.name.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE))
                    } ?: query } )
                .sort(on<SortHandler>().sortPhones())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .let { if (single) it.single() else it }
                .observer {
            callback(it)
        }
    }

    fun suggestions(latLng: LatLng, queryString: String? = null, single: Boolean = false, callback: (List<Suggestion>) -> Unit): DataSubscription {
        val distance = on<HowFar>().about7Miles

        return on<StoreHandler>().store.box(Suggestion::class).query(
                Suggestion_.latitude.between(latLng.latitude - distance, latLng.latitude + distance).and(
                Suggestion_.longitude.between(latLng.longitude - distance, latLng.longitude + distance)).let { query ->
                    queryString?.takeIf { it.isNotBlank() }?.let {
                        query.and(Suggestion_.name.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE))
                    } ?: query } )
                .sort(on<SortHandler>().sortSuggestions(latLng))
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .let { if (single) it.single() else it }
                .observer {
            callback(it)
        }
    }

    fun events(latLng: LatLng, queryString: String? = null, single: Boolean = false, callback: (List<Event>) -> Unit): DataSubscription {
        val distance = on<HowFar>().about7Miles

        return on<StoreHandler>().store.box(Event::class).query(
                Event_.startsAt.less(on<TimeAgo>().startOfToday(1))
                        .and(Event_.endsAt.greater(Date()))
                        .and(Event_.latitude.between(latLng.latitude - distance, latLng.latitude + distance)
                        .and(Event_.longitude.between(latLng.longitude - distance, latLng.longitude + distance))).let { query ->
                            queryString?.takeIf { it.isNotBlank() }?.let {
                                query.and(Event_.about.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE) or
                                        Event_.name.contains(queryString, QueryBuilder.StringOrder.CASE_INSENSITIVE))
                            } ?: query } )
                .sort(on<SortHandler>().sortEvents(latLng))
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .let { if (single) it.single() else it }
                .observer {
                    callback(it)
                }

    }

    fun groupActions(groups: List<Group>? = null, queryString: String? = null, single: Boolean = false, callback: (List<GroupAction>) -> Unit) = on<StoreHandler>().store.box(GroupAction::class).query(
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
            .let { if (single) it.single() else it }
            .observer { callback(it) }!!
}
