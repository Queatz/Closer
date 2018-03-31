package closer.vlllage.com.closer.api.models;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.store.models.Suggestion;

public class SuggestionResult {
    public String id;
    public List<Double> geo;
    public String name;

    public static List<Suggestion> from(List<SuggestionResult> suggestionResults) {
        List<Suggestion> result = new ArrayList<>();

        for (SuggestionResult suggestionResult : suggestionResults) {
            Suggestion suggestion = new Suggestion();
            suggestion.setId(suggestionResult.id);
            suggestion.setName(suggestionResult.name);
            suggestion.setLatitude(suggestionResult.geo.get(0));
            suggestion.setLongitude(suggestionResult.geo.get(1));
            result.add(suggestion);
        }

        return result;
    }
}
