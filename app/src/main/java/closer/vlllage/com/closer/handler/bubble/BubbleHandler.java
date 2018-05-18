package closer.vlllage.com.closer.handler.bubble;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import closer.vlllage.com.closer.pool.PoolMember;

public class BubbleHandler extends PoolMember {

    private MapBubbleView.OnMapBubbleClickListener onClickListener;
    private MapBubbleMenuView.OnMapBubbleMenuItemClickListener onMenuItemClickListener;
    private MapBubbleSuggestionView.MapBubbleSuggestionClickListener onMapBubbleSuggestionClickListener;
    private MapBubbleEventView.MapBubbleEventClickListener onMapBubbleEventClickListener;
    private MapBubblePhysicalGroupView.MapBubblePhysicalGroupClickListener onMapBubblePhysicalGroupClickListener;

    private final BubbleMapLayer bubbleMapLayer = new BubbleMapLayer();

    public void attach(ViewGroup bubbleMapLayerLayout,
                       MapBubbleView.OnMapBubbleClickListener onClickListener,
                       MapBubbleMenuView.OnMapBubbleMenuItemClickListener onMenuItemClickListener,
                       MapBubbleEventView.MapBubbleEventClickListener onMapBubbleEventClickListener,
                       MapBubbleSuggestionView.MapBubbleSuggestionClickListener onMapBubbleSuggestionClickListener,
                       MapBubblePhysicalGroupView.MapBubblePhysicalGroupClickListener onMapBubblePhysicalGroupClickListener) {
        this.onClickListener = onClickListener;
        this.onMenuItemClickListener = onMenuItemClickListener;
        this.onMapBubbleEventClickListener = onMapBubbleEventClickListener;
        this.onMapBubbleSuggestionClickListener = onMapBubbleSuggestionClickListener;
        this.onMapBubblePhysicalGroupClickListener = onMapBubblePhysicalGroupClickListener;
        bubbleMapLayer.attach(bubbleMapLayerLayout, getBubbleView());
    }

    public void attach(GoogleMap map) {
        bubbleMapLayer.attach(map);
    }

    private BubbleMapLayer.BubbleView getBubbleView() {
        return new BubbleMapLayer.BubbleView() {
            @Override
            public View createView(ViewGroup view, MapBubble mapBubble) {
                switch (mapBubble.getType()) {
                    case MENU:
                        return $(MapBubbleMenuView.class).from(view, mapBubble, onMenuItemClickListener);
                    case SUGGESTION:
                        return $(MapBubbleSuggestionView.class).from(view, mapBubble, onMapBubbleSuggestionClickListener);
                    case EVENT:
                        return $(MapBubbleEventView.class).from(view, mapBubble, onMapBubbleEventClickListener);
                    case PHYSICAL_GROUP:
                        return $(MapBubblePhysicalGroupView.class).from(view, mapBubble, onMapBubblePhysicalGroupClickListener);
                    default:
                        return $(MapBubbleView.class).from(view, mapBubble, onClickListener);
                }
            }

            @Override
            public void updateView(MapBubble mapBubble) {
                switch (mapBubble.getType()) {
                    case STATUS:
                        $(MapBubbleView.class).update(mapBubble.getView(), mapBubble);
                        break;
                }
            }
        };
    }

    public void add(final MapBubble mapBubble) {
        bubbleMapLayer.add(mapBubble);
    }

    public void replace(List<MapBubble> mapBubbles) {
        bubbleMapLayer.replace(mapBubbles);
    }

    public void update() {
        bubbleMapLayer.update();
    }

    public void update(MapBubble mapBubble) {
        bubbleMapLayer.update(mapBubble);
    }

    public void move(MapBubble mapBubble, LatLng latLng) {
        bubbleMapLayer.move(mapBubble, latLng);
    }

    public void updateDetails(MapBubble mapBubble) {
        bubbleMapLayer.updateDetails(mapBubble);
    }

    public void remove(MapBubble mapBubble) {
        bubbleMapLayer.remove(mapBubble);
    }

    public void remove(BubbleMapLayer.RemoveCallback callback) {
        bubbleMapLayer.remove(callback);
    }
}
