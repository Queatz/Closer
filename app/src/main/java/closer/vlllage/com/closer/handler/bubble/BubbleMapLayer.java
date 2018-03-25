package closer.vlllage.com.closer.handler.bubble;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jacob on 2/18/18.
 */

public class BubbleMapLayer {

    private final Set<MapBubble> mapBubbles = new HashSet<>();
    private final Map<MapBubble, Animator> mapBubbleAnimations = new HashMap<>();
    private final Map<MapBubble, ViewPropertyAnimator> mapBubbleAppearDisappearAnimations = new HashMap<>();
    private GoogleMap map;
    private ViewGroup view;
    private MapBubbleView.OnMapBubbleClickListener onClickListener;
    private MapBubbleMenuView.OnMapBubbleMenuItemClickListener onMenuItemClickListener;
    private MapBubbleSuggestionView.MapBubbleSuggestionClickListener onMapBubbleSuggestionClickListener;

    public void attach(GoogleMap map, ViewGroup view,
                       MapBubbleView.OnMapBubbleClickListener onClickListener,
                       MapBubbleMenuView.OnMapBubbleMenuItemClickListener onMenuItemClickListener,
                       MapBubbleSuggestionView.MapBubbleSuggestionClickListener onMapBubbleSuggestionClickListener) {
        this.map = map;
        this.view = view;
        this.onClickListener = onClickListener;
        this.onMenuItemClickListener = onMenuItemClickListener;
        this.onMapBubbleSuggestionClickListener = onMapBubbleSuggestionClickListener;
    }

    public void add(MapBubble mapBubble) {
        if (mapBubbles.contains(mapBubble)) {
            return;
        }

        mapBubbles.add(mapBubble);

        switch (mapBubble.getType()) {
            case MENU:
                mapBubble.setView(MapBubbleMenuView.from(view, mapBubble, onMenuItemClickListener));
                break;
            case SUGGESTION:
                mapBubble.setView(MapBubbleSuggestionView.from(view, mapBubble, onMapBubbleSuggestionClickListener));
                break;
            default:
                mapBubble.setView(MapBubbleView.from(view, mapBubble, onClickListener));
                break;
        }

        view.addView(mapBubble.getView());

        mapBubble.getView().setScaleX(0);
        mapBubble.getView().setScaleY(0);

        view.post(() -> {
            mapBubble.getView().setPivotX(mapBubble.getView().getWidth() / 2);
            mapBubble.getView().setPivotY(mapBubble.getView().getHeight());

            ViewPropertyAnimator animator = mapBubble.getView().animate()
                    .scaleX(zoomScale(mapBubble))
                    .scaleY(zoomScale(mapBubble))
                    .setInterpolator(new OvershootInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mapBubbleAppearDisappearAnimations.remove(mapBubble);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .setDuration(195);

            mapBubbleAppearDisappearAnimations.put(mapBubble, animator);


            animator.start();

            update(mapBubble);
        });
    }

    public void update() {
        for (MapBubble mapBubble : mapBubbles) {
            update(mapBubble);
        }
    }

    public void update(MapBubble mapBubble) {
        View view = mapBubble.getView();

        if (view == null) {
            return;
        }

        Point point = map.getProjection().toScreenLocation(mapBubble.getLatLng());
        view.setX(point.x - view.getWidth() / 2);
        view.setY(point.y - view.getHeight());

        if (!mapBubbleAppearDisappearAnimations.containsKey(mapBubble)) {
            mapBubble.getView().setScaleX(zoomScale(mapBubble));
            mapBubble.getView().setScaleY(zoomScale(mapBubble));
        }

        view.setElevation((mapBubble.isOnTop() ? 2 : 1) + (float) point.y / (float) this.view.getHeight());
    }

    public void updateDetails(MapBubble mapBubble) {
        if (mapBubble.getView() == null) {
            return;
        }

        MapBubbleView.update(mapBubble.getView(), mapBubble);
        mapBubble.getView().setPivotX(mapBubble.getView().getWidth() / 2);
        mapBubble.getView().setPivotY(mapBubble.getView().getHeight());
        view.post(() -> update(mapBubble));
    }

    public void move(final MapBubble mapBubble, final LatLng targetLatLng) {
        if (mapBubbleAnimations.containsKey(mapBubble)) {
            Animator activeAnimator = mapBubbleAnimations.get(mapBubble);
            if (activeAnimator.isRunning()) {
                activeAnimator.cancel();
            }
        }

        final LatLng sourceLatLng = new LatLng(mapBubble.getLatLng().latitude, mapBubble.getLatLng().longitude);

        ValueAnimator animator = new ValueAnimator();
        animator.setDuration(1000).setFloatValues(0f, 1f);

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = animation.getAnimatedFraction();

            mapBubble.setLatLng(new LatLng(
                    sourceLatLng.latitude * (1 - value) + targetLatLng.latitude * value,
                    sourceLatLng.longitude * (1 - value) + targetLatLng.longitude * value
            ));
            update(mapBubble);
        });

        animator.start();

        mapBubbleAnimations.put(mapBubble, animator);
    }

    public void clear() {
        for (MapBubble mapBubble : mapBubbles) {
            if (mapBubble.isPinned()) {
                continue;
            }

            remove(mapBubble);
        }
    }

    public void remove(MapBubble mapBubble) {
        if (!mapBubbles.contains(mapBubble)) {
            return;
        }

        view.post(() -> {
            ViewPropertyAnimator animator = mapBubble.getView()
                    .animate()
                    .scaleX(0)
                    .scaleY(0)
                    .setInterpolator(new AnticipateInterpolator())
                    .setDuration(225)
                    .setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mapBubbles.remove(mapBubble);
                    view.removeView(mapBubble.getView());
                    mapBubble.setView(null);
                    mapBubbleAppearDisappearAnimations.remove(mapBubble);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            mapBubbleAppearDisappearAnimations.put(mapBubble, animator);

            animator.start();
        });
    }

    public void replace(List<MapBubble> mapBubbles) {
        Map<String, MapBubble> byPhone = new HashMap<>();

        for (MapBubble mapBubble : mapBubbles) {
            if (mapBubble.getPhone() != null) {
                byPhone.put(mapBubble.getPhone(), mapBubble);
            }
        }

        Set<String> updatedBubbles = new HashSet<>();

        for (MapBubble mapBubble : this.mapBubbles) {
            if (mapBubble.isPinned()) {
                continue;
            }

            if (mapBubble.getPhone() == null || !byPhone.containsKey(mapBubble.getPhone())) {
                remove(mapBubble);
            } else {

                mapBubble.updateFrom(byPhone.get(mapBubble.getPhone()));
                move(mapBubble, byPhone.get(mapBubble.getPhone()).getLatLng());
                updateDetails(mapBubble);
                updatedBubbles.add(mapBubble.getPhone());
            }
        }

        for (MapBubble mapBubble : mapBubbles) {
            if (mapBubble.getPhone() != null && !updatedBubbles.contains(mapBubble.getPhone())) {
                add(mapBubble);
            }
        }
    }

    private float zoomScale(MapBubble mapBubble) {
        if (mapBubble.getType() == BubbleType.STATUS) {
            return (float) Math.min(1, Math.pow(map.getCameraPosition().zoom / 15f, 2f));
        }

        return 1;
    }
}
