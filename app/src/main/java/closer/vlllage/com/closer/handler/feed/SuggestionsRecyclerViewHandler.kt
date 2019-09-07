package closer.vlllage.com.closer.handler.feed

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.handler.helpers.TimerHandler
import closer.vlllage.com.closer.store.models.Suggestion
import com.queatz.on.On

class SuggestionsRecyclerViewHandler constructor(private val on: On) {

    private lateinit var suggestionsRecyclerView: RecyclerView
    private val suggestionsAdapter = SuggestionsAdapter(on)

    fun attach(suggestionsRecyclerView: RecyclerView) {
        this.suggestionsRecyclerView = suggestionsRecyclerView
        suggestionsRecyclerView.adapter = suggestionsAdapter
        suggestionsRecyclerView.layoutManager = LinearLayoutManager(
                suggestionsRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )
    }

    fun setSuggestions(suggestions: MutableList<Suggestion>) {
        suggestionsAdapter.suggestions = suggestions
        on<TimerHandler>().post(Runnable { suggestionsRecyclerView.scrollBy(0, 0) })
    }
}
