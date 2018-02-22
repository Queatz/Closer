package closer.vlllage.com.closer;

import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.google.android.gms.maps.GoogleMap;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jacob on 2/18/18.
 */

public class BubbleMapLayer {

    private final Set<MapBubble> mapBubbles = new HashSet<>();
    private GoogleMap map;
    private ViewGroup view;
    private MapBubbleView.OnMapBubbleClickListener onClickListener;

    public void attach(GoogleMap map, ViewGroup view, MapBubbleView.OnMapBubbleClickListener onClickListener) {
        this.map = map;
        this.view = view;
        this.onClickListener = onClickListener;
    }

    public void add(MapBubble mapBubble) {
        mapBubbles.add(mapBubble);
        mapBubble.setView(MapBubbleView.from(view, mapBubble, onClickListener));
        view.addView(mapBubble.getView());
        update();

        mapBubble.getView().setScaleX(0);
        mapBubble.getView().setScaleY(0);

        view.post(() -> {
            mapBubble.getView().setPivotX(mapBubble.getView().getWidth() / 2);
            mapBubble.getView().setPivotY(mapBubble.getView().getHeight());
            mapBubble.getView().animate().scaleX(1).scaleY(1).setInterpolator(new OvershootInterpolator()).setDuration(195).start();
        });
    }

    public void update() {
        for (MapBubble mapBubble : mapBubbles) {
            View view = mapBubble.getView();

            if (view == null) {
                continue;
            }

            Point point = map.getProjection().toScreenLocation(mapBubble.getLatLng());
            view.setX(point.x - view.getWidth() / 2);
            view.setY(point.y - view.getHeight());
            view.setElevation(1 + (float) point.y / (float) this.view.getHeight());
        }
    }
}
