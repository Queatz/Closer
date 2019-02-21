package closer.vlllage.com.closer.api.models;

import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.store.models.Suggestion;

public class SuggestionResult extends ModelResult {
    public List<Double> geo;
    public String name;
    public Date created;

    public static Suggestion from(SuggestionResult suggestionResult) {
            Suggestion suggestion = new Suggestion();
            suggestion.setId(suggestionResult.id);
            updateFrom(suggestion, suggestionResult);
        return suggestion;
    }

    public static Suggestion updateFrom(Suggestion suggestion, SuggestionResult suggestionResult) {
        suggestion.setName(suggestionResult.name);
        suggestion.setLatitude(suggestionResult.geo.get(0));
        suggestion.setLongitude(suggestionResult.geo.get(1));
        suggestion.setCreated(suggestionResult.created);
        return suggestion;
    }
}
