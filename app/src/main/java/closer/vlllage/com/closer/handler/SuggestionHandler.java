package closer.vlllage.com.closer.handler;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.pool.PoolMember;

public class SuggestionHandler extends PoolMember {

    private final Set<MapBubble> suggestionBubbles = new HashSet<>();

    private final static String[] suggestions = new String[] {
            "Dance at Rose Room",
            "Hot yoga at Kathleane",
            "Nude photoshoot at the beach",
            "Relax all day at Zilker",
            "Dance at Kingdom and Empire",
            "See if we can sleep over at Jacob & Mai's",
    };

    public void shuffle() {
        clearSuggestions();

        Set<MapBubble> nextBubbles = new HashSet<>();

        for (int i = 0; i < 3; i++) {
            LatLng latLng = $(MapHandler.class).getCenter();
            latLng = new LatLng(latLng.latitude + (Math.random() - .5) * .1, latLng.longitude + (Math.random() - .5) * .1);

            MapBubble suggestionBubble = new MapBubble(latLng, "Suggestion", suggestions[new Random().nextInt(suggestions.length)]);
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
}
