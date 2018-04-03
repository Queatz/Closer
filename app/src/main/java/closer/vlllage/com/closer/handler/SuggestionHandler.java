package closer.vlllage.com.closer.handler;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Suggestion;
import closer.vlllage.com.closer.store.models.Suggestion_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.SubscriptionBuilder;

public class SuggestionHandler extends PoolMember {

    private final Set<MapBubble> suggestionBubbles = new HashSet<>();

    public void shuffle() {
        $(BubbleHandler.class).remove(mapBubble -> BubbleType.MENU.equals(mapBubble.getType()));
        clearSuggestions();

        Set<MapBubble> nextBubbles = new HashSet<>();

        LatLng mapCenter = $(MapHandler.class).getCenter();

        getRandomSuggestions(mapCenter).observer(suggestions -> {
            if (suggestions.isEmpty()) {
                return;
            }

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

                $(TimerHandler.class).postDisposable(() -> {
                    $(BubbleHandler.class).add(suggestionBubble);
                    suggestionBubbles.add(suggestionBubble);
                }, 225 * 2 + i * 95);

                nextBubbles.add(suggestionBubble);
            }

            $(MapHandler.class).centerOn(nextBubbles);
        });
    }

    private SubscriptionBuilder<List<Suggestion>> getRandomSuggestions(LatLng near) {
        return $(StoreHandler.class).getStore().box(Suggestion.class).query().build().subscribe().single().on(AndroidScheduler.mainThread());
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
        $(AlertHandler.class).make()
            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.add_suggestion_here))
            .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.add_suggestion))
            .setLayoutResId(R.layout.make_suggestion_modal)
            .setTextView(R.id.input, result -> createNewSuggestion(latLng, result))
            .show();
    }

    private void createNewSuggestion(LatLng latLng, String name) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        Suggestion suggestion = $(StoreHandler.class).create(Suggestion.class);
        suggestion.setName(name.trim());
        suggestion.setLatitude(latLng.latitude);
        suggestion.setLongitude(latLng.longitude);
        $(StoreHandler.class).getStore().box(Suggestion.class).put(suggestion);
        $(SyncHandler.class).sync(suggestion);
    }

    public void loadAll(List<Suggestion> suggestions) {
        for (Suggestion suggestion : suggestions) {
            $(StoreHandler.class).getStore().box(Suggestion.class).query()
                    .equal(Suggestion_.id, suggestion.getId())
                    .build().subscribe().single().on(AndroidScheduler.mainThread())
                    .observer(result -> {
                        if (result.isEmpty()) {
                            $(StoreHandler.class).getStore().box(Suggestion.class).put(suggestion);
                        }
            });
        }
    }
}
