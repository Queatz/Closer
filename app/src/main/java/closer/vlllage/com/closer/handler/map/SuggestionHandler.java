package closer.vlllage.com.closer.handler.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.api.models.SuggestionResult;
import closer.vlllage.com.closer.handler.bubble.BubbleHandler;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.TimeStr;
import closer.vlllage.com.closer.handler.helpers.TimerHandler;
import closer.vlllage.com.closer.handler.helpers.ToastHandler;
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

        getRandomSuggestions($(MapHandler.class).getVisibleRegion().latLngBounds).observer(suggestions -> {
            if (suggestions.isEmpty()) {
                $(ToastHandler.class).show(R.string.no_suggestions_here);
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

                MapBubble suggestionBubble = suggestionBubbleFrom(suggestion);

                $(TimerHandler.class).postDisposable(() -> {
                    $(BubbleHandler.class).add(suggestionBubble);
                    suggestionBubbles.add(suggestionBubble);
                }, 225 * 2 + i * 95);

                nextBubbles.add(suggestionBubble);
            }

            $(MapHandler.class).centerOn(nextBubbles);
        });
    }

    public MapBubble suggestionBubbleFrom(Suggestion suggestion) {
        MapBubble suggestionBubble = new MapBubble(new LatLng(
                suggestion.getLatitude(),
                suggestion.getLongitude()
        ), $(ResourcesHandler.class).getResources().getString(R.string.suggestion), suggestion.getName());
        suggestionBubble.setPinned(true);
        suggestionBubble.setOnTop(true);
        suggestionBubble.setType(BubbleType.SUGGESTION);
        suggestionBubble.setTag(suggestion);
        suggestionBubble.setAction($(TimeStr.class).prettyDate(suggestion.getCreated()));
        return suggestionBubble;
    }

    private SubscriptionBuilder<List<Suggestion>> getRandomSuggestions(LatLngBounds bounds) {
        return $(StoreHandler.class).getStore().box(Suggestion.class).query()
                .between(Suggestion_.latitude, bounds.southwest.latitude, bounds.northeast.latitude)
                .between(Suggestion_.longitude, bounds.southwest.longitude, bounds.northeast.longitude)
                .build().subscribe().single().on(AndroidScheduler.mainThread());
    }

    public boolean clearSuggestions() {
        boolean anyBubblesRemoved = $(BubbleHandler.class).remove(mapBubble -> BubbleType.SUGGESTION.equals(mapBubble.getType()));
        suggestionBubbles.clear();
        return anyBubblesRemoved;
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

    public void loadAll(List<SuggestionResult> suggestions) {
        for (SuggestionResult suggestionResult : suggestions) {
            $(StoreHandler.class).getStore().box(Suggestion.class).query()
                    .equal(Suggestion_.id, suggestionResult.id)
                    .build().subscribe().single().on(AndroidScheduler.mainThread())
                    .observer(result -> {
                        if (result.isEmpty()) {
                            $(StoreHandler.class).getStore().box(Suggestion.class).put(SuggestionResult.from(suggestionResult));
                        } else {
                            $(StoreHandler.class).getStore().box(Suggestion.class).put(
                                    SuggestionResult.updateFrom(result.get(0), suggestionResult));
                        }
            });
        }
    }
}
