package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.SuggestionResult
import closer.vlllage.com.closer.handler.bubble.BubbleHandler
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.bubble.MapBubble
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.SuggestionBubbleHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Suggestion
import closer.vlllage.com.closer.store.models.Suggestion_
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.SubscriptionBuilder
import java.util.*

class SuggestionHandler constructor(private val on: On) {

    private val suggestionBubbles = HashSet<MapBubble>()

    fun shuffle() {
        on<BubbleHandler>().remove { mapBubble -> BubbleType.MENU == mapBubble.type }
        clearSuggestions()

        getRandomSuggestions(on<MapHandler>().visibleRegion!!.latLngBounds).observer { suggestions ->
            if (suggestions.isEmpty()) {
                on<ToastHandler>().show(R.string.no_suggestions_here)
                return@observer
            }

            val random = Random()
            val suggested = HashSet<Suggestion>()
            for (i in 0..2) {
                val suggestion = suggestions[random.nextInt(suggestions.size)]

                if (suggested.contains(suggestion)) {
                    continue
                }

                suggested.add(suggestion)
            }

            showSuggestions(suggestions)
        }
    }

    fun showSuggestions(suggestions: Collection<Suggestion>) {
        val bubbles = suggestions.map(this::suggestionBubbleFrom)

        bubbles.forEachIndexed { index, bubble ->
            on<TimerHandler>().postDisposable({
                on<BubbleHandler>().add(bubble)
                suggestionBubbles.add(bubble)
            }, (225 * 2 + index * 95).toLong())
        }


        on<MapHandler>().centerOn(bubbles)
    }

    fun suggestionBubbleFrom(suggestion: Suggestion): MapBubble {
        val suggestionBubble = MapBubble(LatLng(
                suggestion.latitude!!,
                suggestion.longitude!!
        ), on<ResourcesHandler>().resources.getString(R.string.suggestion), suggestion.name)
        suggestionBubble.isPinned = true
        suggestionBubble.type = BubbleType.SUGGESTION
        suggestionBubble.tag = suggestion
        suggestionBubble.action = on<TimeStr>().prettyDate(suggestion.created)
        return suggestionBubble
    }

    private fun getRandomSuggestions(bounds: LatLngBounds): SubscriptionBuilder<List<Suggestion>> {
        return on<StoreHandler>().store.box(Suggestion::class).query()
                .between(Suggestion_.latitude, bounds.southwest.latitude, bounds.northeast.latitude)
                .between(Suggestion_.longitude, bounds.southwest.longitude, bounds.northeast.longitude)
                .build().subscribe().single().on(AndroidScheduler.mainThread())
    }

    fun clearSuggestions() {
        on<SuggestionBubbleHandler>().clear()
        val anyBubblesRemoved = on<BubbleHandler>().remove { mapBubble -> BubbleType.SUGGESTION == mapBubble.type }
        suggestionBubbles.clear()
    }

    fun createNewSuggestion(latLng: LatLng) {
        on<AlertHandler>().make().apply {
            title = on<ResourcesHandler>().resources.getString(R.string.add_a_suggestion)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.add_suggestion)
            layoutResId = R.layout.make_suggestion_modal
            textViewId = R.id.input
            onTextViewSubmitCallback = { result -> createNewSuggestion(latLng, result) }
            show()
        }
    }

    private fun createNewSuggestion(latLng: LatLng, name: String?) {
        if (name == null || name.isBlank()) {
            return
        }

        val suggestion = on<StoreHandler>().create(Suggestion::class.java)
        suggestion!!.name = name.trim()
        suggestion.latitude = latLng.latitude
        suggestion.longitude = latLng.longitude
        on<StoreHandler>().store.box(Suggestion::class).put(suggestion)
        on<SyncHandler>().sync(suggestion)
    }

    fun loadAll(suggestions: List<SuggestionResult>) {
        for (suggestionResult in suggestions) {
            on<StoreHandler>().store.box(Suggestion::class).query()
                    .equal(Suggestion_.id, suggestionResult.id!!)
                    .build().subscribe().single().on(AndroidScheduler.mainThread())
                    .observer { result ->
                        if (result.isEmpty()) {
                            on<StoreHandler>().store.box(Suggestion::class).put(SuggestionResult.from(suggestionResult))
                        } else {
                            on<StoreHandler>().store.box(Suggestion::class).put(
                                    SuggestionResult.updateFrom(result[0], suggestionResult))
                        }
                    }
        }
    }
}
