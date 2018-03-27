package closer.vlllage.com.closer.handler;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Suggestion;
import io.objectbox.query.Query;

public class SuggestionHandler extends PoolMember {

    private final Set<MapBubble> suggestionBubbles = new HashSet<>();

    public void shuffle() {
        $(BubbleHandler.class).remove(mapBubble -> BubbleType.MENU.equals(mapBubble.getType()));
        clearSuggestions();

        Set<MapBubble> nextBubbles = new HashSet<>();

        LatLng mapCenter = $(MapHandler.class).getCenter();

        getRandomSuggestions(mapCenter).subscribe().observer(suggestions -> {
            Random random = new Random();
            Set<Suggestion> suggested = new HashSet<>();
            for (int i = 0; i < 3; i++) {
                Suggestion suggestion = suggestions.get(random.nextInt(suggestions.size()));

                if (suggestion == null) {
                    continue;
                }

                if (suggested.contains(suggestion)) {
                    continue;
                }

                suggested.add(suggestion);

                MapBubble suggestionBubble = new MapBubble(new LatLng(
                        suggestion.getLatitude(),
                        suggestion.getLongitude()
                ), "Suggestion", suggestion.getName());
                suggestionBubble.setPinned(true);
                suggestionBubble.setOnTop(true);
                suggestionBubble.setType(BubbleType.SUGGESTION);

                $(TimerHandler.class).post(() -> {
                    $(BubbleHandler.class).add(suggestionBubble);
                    suggestionBubbles.add(suggestionBubble);
                }, 225 * 2 + i * 95);

                nextBubbles.add(suggestionBubble);
            }

            $(MapHandler.class).centerOn(nextBubbles);
        });
    }

    private Query<Suggestion> getRandomSuggestions(LatLng near) {
        return $(StoreHandler.class).getStore().box(Suggestion.class).query().build();
    }

    public void clearSuggestions() {
        for (MapBubble mapBubble : suggestionBubbles) {
            $(BubbleHandler.class).remove(mapBubble);
        }

        suggestionBubbles.clear();
    }

    public void attach(View shuffleButton) {
        shuffleButton.setOnClickListener(view -> this.shuffle());
    }

    public void createNewSuggestion(final LatLng latLng) {
        $(AlertHandler.class).makeAlert(String.class)
            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.add_suggestion_here))
            .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.add_suggestion))
            .setLayoutResId(R.layout.set_name_modal)
            .setTextView(R.id.input, result -> createNewSuggestion(latLng, result))
            .show();
    }

    private void createNewSuggestion(LatLng latLng, String name) {
        if (name == null || name.isEmpty()) {
            return;
        }

        Suggestion suggestion = $(StoreHandler.class).create(Suggestion.class);
        suggestion.setName(name);
        suggestion.setLatitude(latLng.latitude);
        suggestion.setLongitude(latLng.longitude);
        $(StoreHandler.class).getStore().box(Suggestion.class).put(suggestion);
    }
}
