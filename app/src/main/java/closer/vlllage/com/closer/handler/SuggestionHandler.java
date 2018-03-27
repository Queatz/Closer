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
import io.realm.RealmResults;

public class SuggestionHandler extends PoolMember {

    private final Set<MapBubble> suggestionBubbles = new HashSet<>();

    public void shuffle() {
        $(BubbleHandler.class).remove(mapBubble -> BubbleType.MENU.equals(mapBubble.getType()));
        clearSuggestions();

        Set<MapBubble> nextBubbles = new HashSet<>();

        LatLng mapCenter = $(MapHandler.class).getCenter();

        getRandomSuggestions(mapCenter).addChangeListener(results -> {
            results.removeAllChangeListeners();

            Random random = new Random();
            Set<Suggestion> suggested = new HashSet<>();
            for (int i = 0; i < 3; i++) {
                Suggestion suggestion = results.get(random.nextInt(results.size()));

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

    private RealmResults<Suggestion> getRandomSuggestions(LatLng near) {
        return $(StoreHandler.class).getStore().getRealm().where(Suggestion.class).findAllAsync();
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
            .setPositiveButtonCallback(result -> createNewSuggestion(latLng, result.getResult()))
            .show();
    }

    private void createNewSuggestion(LatLng latLng, String name) {
        if (name == null || name.isEmpty()) {
            return;
        }

        Suggestion suggestion = $(StoreHandler.class).create(Suggestion.class);
        $(StoreHandler.class).execute(transaction -> {
            suggestion.setName(name);
            suggestion.setLatitude(latLng.latitude);
            suggestion.setLongitude(latLng.longitude);
        });
    }
}
