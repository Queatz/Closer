package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.bubble.BubbleHandler
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.AccountHandler.Companion.ACCOUNT_FIELD_PRIVATE
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.handler.map.MapZoomHandler
import closer.vlllage.com.closer.handler.map.SuggestionHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Suggestion
import closer.vlllage.com.closer.store.models.Suggestion_
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import java.util.*


class SuggestionBubbleHandler constructor(private val on: On) {

    private val visibleSuggestions = HashSet<String>()
    private val disposableGroup = on<DisposableHandler>().group()

    private fun update() {
        val distance = .12f

        disposableGroup.clear()

        if (on<AccountHandler>().privateOnly) {
            visibleSuggestions.clear()
            clearBubbles()
            return
        }

        disposableGroup.add(on<MapHandler>().onMapIdleObservable().subscribe {
                val queryBuilder = on<StoreHandler>().store.box(Suggestion::class).query()
                        .between(Suggestion_.latitude, it.target.latitude - distance, it.target.latitude + distance)
                        .and()
                        .between(Suggestion_.longitude, it.target.longitude - distance, it.target.longitude + distance)

                disposableGroup.add(queryBuilder
                        .sort(on<SortHandler>().sortSuggestions(it.target))
                        .build()
                        .subscribe()
                        .on(AndroidScheduler.mainThread())
                        .single()
                        .observer { suggestions ->
                            for (suggestion in suggestions.take(3)) {
                                if (!visibleSuggestions.contains(suggestion.id)) {
                                    val mapBubble = on<SuggestionHandler>().suggestionBubbleFrom(suggestion)
                                    on<BubbleHandler>().add(mapBubble)
                                }
                            }

                            visibleSuggestions.clear()

                            for (suggestion in suggestions.take(3)) {
                                if (suggestion.id == null) {
                                    continue
                                }
                                visibleSuggestions.add(suggestion.id!!)
                            }

                            clearBubbles()
                        })

            })
        }

    fun attach() {
        on<DisposableHandler>().add(on<AccountHandler>().changes(ACCOUNT_FIELD_PRIVATE).subscribe {
            update()
        })
        on<DisposableHandler>().add(on<MapZoomHandler>().onZoomGreaterThanChanged(GEO_SUGGESTIONS_ZOOM).subscribe(
                { zoomIsGreaterThan15 ->
                    if (zoomIsGreaterThan15) {
                        update()
                    } else {
                        disposableGroup.clear()
                        visibleSuggestions.clear()
                        clearBubbles()
                    }

                }, { it.printStackTrace() }
        ))
    }

    private fun clearBubbles() {
        on<BubbleHandler>().remove { mapBubble -> mapBubble.type == BubbleType.SUGGESTION && !visibleSuggestions.contains((mapBubble.tag as Suggestion).id) }
    }

    fun clear() {
        visibleSuggestions.clear()
        clearBubbles()
    }

    companion object {
        private const val GEO_SUGGESTIONS_ZOOM = 15f
    }
}
