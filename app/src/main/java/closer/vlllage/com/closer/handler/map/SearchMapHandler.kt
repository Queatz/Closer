package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.Search
import closer.vlllage.com.closer.handler.helpers.ToastHandler
import closer.vlllage.com.closer.store.models.*
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.on.On

class SearchMapHandler constructor(private val on: On) {
    private var shouldRestartSearch = false
    private var lastQuery: String = ""
    private var lastResults: SearchResults = SearchResults {}
    private var lastResultsCursor = 0
    private var disposableGroup = on<DisposableHandler>().group()

    fun next(query: String) {
        if (!shouldRestartSearch && lastQuery == query && lastResults.ready) {
            continueSearch()
        } else {
            shouldRestartSearch = false
            on<MapHandler>().center ?: return

            startSearch(query, on<MapHandler>().center!!)
        }
    }

    private fun continueSearch() {
        lastResults.get(lastResultsCursor)?.let {
            when (it) {
                is Phone -> { on<MapHandler>().centerMap(LatLng(it.latitude!!, it.longitude!!), 20f) }
                is Event -> { on<MapHandler>().centerMap(LatLng(it.latitude!!, it.longitude!!), 20f) }
                is Group -> { on<MapHandler>().centerMap(LatLng(it.latitude!!, it.longitude!!), 20f) }
                is Suggestion -> { on<MapHandler>().centerMap(LatLng(it.latitude!!, it.longitude!!), 20f) }
            }
            lastResultsCursor++
        } ?: noResults()
    }

    private fun noResults() {
        on<ToastHandler>().show(if (lastResultsCursor > 0) R.string.no_more_results else R.string.no_results, lastResultsCursor > 0)
        if (lastResultsCursor > 0) on<MapHandler>().zoomMap(-1f)

        shouldRestartSearch = true
    }

    private fun startSearch(query: String, center: LatLng) {
        lastResultsCursor = 0
        lastQuery = query
        lastResults = SearchResults { continueSearch() }
        disposableGroup.clear()

        on<Search>().phones(center, query, true) { lastResults.phones = it }.also { disposableGroup.add(it) }
        on<Search>().physicalGroups(center, query, privateOnly = false, single = true) { lastResults.groups = it }.also { disposableGroup.add(it) }
        on<Search>().events(center, query, true) { lastResults.events = it }.also { disposableGroup.add(it) }
        on<Search>().suggestions(center, query, true) { lastResults.suggestions = it }.also { disposableGroup.add(it) }
    }

}

private class SearchResults(private val onReady: () -> Unit) {
    var phones: List<Phone>? = null
        set(value) { field = value; check()}
    var events: List<Event>? = null
        set(value) { field = value; check()}
    var groups: List<Group>? = null
        set(value) { field = value; check()}
    var suggestions: List<Suggestion>? = null
        set(value) { field = value; check()}

    val ready: Boolean get() = listOf(phones, groups, events, suggestions).all { it != null }

    fun get(index: Int): BaseObject? {
        var offset = 0

        val set = listOf(phones!!, events!!, groups!!, suggestions!!).firstOrNull {
            if (index - offset < it.size) {
                true
            } else {
                offset += it.size
                false
            }
        }

        return set?.get(index - offset)
    }

    private fun check() {
        if (ready) onReady()
    }
}
