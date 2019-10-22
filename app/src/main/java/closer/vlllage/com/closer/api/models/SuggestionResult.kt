package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.Suggestion
import java.util.*

class SuggestionResult : ModelResult() {
    var geo: List<Double>? = null
    var name: String? = null

    companion object {

        fun from(suggestionResult: SuggestionResult): Suggestion {
            val suggestion = Suggestion()
            suggestion.id = suggestionResult.id
            updateFrom(suggestion, suggestionResult)
            return suggestion
        }

        fun updateFrom(suggestion: Suggestion, suggestionResult: SuggestionResult): Suggestion {
            suggestion.name = suggestionResult.name
            suggestion.latitude = suggestionResult.geo!![0]
            suggestion.longitude = suggestionResult.geo!![1]
            suggestion.created = suggestionResult.created
            return suggestion
        }
    }
}
