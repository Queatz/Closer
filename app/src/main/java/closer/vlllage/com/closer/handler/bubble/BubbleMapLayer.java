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
import java.util.function.Function;

/**
 * Created by jacob on 2/18/18.
 */

public class BubbleMapLayer {

    private final Set<MapBubble> mapBubbles = new HashSet<>();
    private final Map<MapBubble, Animator> mapBubbleAnimations = new HashMap<>();
    private final Map<MapBubble, ViewPropertyAnimator> mapBubbleAppearDisappearAnimations = new HashMap<>();
    private GoogleMap map;
    private ViewGroup view;
    private BubbleView bubbleView;

    public void attach(ViewGroup view, BubbleView bubbleView) {
        this.view = view;
        this.bubbleView = bubbleView;
    }

    public void attach(GoogleMap map) {
        this.map = map;
        update();
    }

    public void add(MapBubble mapBubble) {
        if (mapBubbles.contains(mapBubble)) {
            return;
        }

        mapBubbles.add(mapBubble);

        mapBubble.setView(bubbleView.createView(view, mapBubble));

        view.addView(mapBubble.getView());

        if (map == null) {
            view.post(() -> update(mapBubble));
            return;
        }

        mapBubble.getView().setScaleX(0);
        mapBubble.getView().setScaleY(0);
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
                        update(mapBubble);
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

        view.post(() -> {
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

        if (map == null) {
            return;
        }

        Point point = map.getProjection().toScreenLocation(mapBubble.getLatLng());
        view.setX(point.x - view.getWidth() / 2);
        view.setY(point.y - view.getHeight());

        mapBubble.getView().setPivotX(mapBubble.getView().getWidth() / 2);
        mapBubble.getView().setPivotY(mapBubble.getView().getHeight());
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

        bubbleView.updateView(mapBubble);
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
            if (mapBubble.getView() == null) {
                return;
            }

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

    public boolean remove(RemoveCallback callback) {
        Set<MapBubble> toRemove = new HashSet<>();
        for (MapBubble mapBubble : mapBubbles) {
            if (callback.apply(mapBubble)) {
                toRemove.add(mapBubble);
            }
        }

        if (toRemove.isEmpty()) {
            return false;
        }

        for (MapBubble mapBubble : toRemove) {
            this.remove(mapBubble);
        }

        return true;
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
        if (mapBubble.getType() == BubbleType.PHYSICAL_GROUP) {
            return (float) Math.pow(map.getCameraPosition().zoom / 15f, 2f);
        }

        if (mapBubble.getType() != BubbleType.MENU) {
            return (float) Math.min(1, Math.pow(map.getCameraPosition().zoom / 15f, 2f));
        }

        return 1;
    }

    public interface BubbleView {
        View createView(ViewGroup view, MapBubble mapBubble);
        void updateView(MapBubble mapBubble);
    }

    public interface RemoveCallback {
        boolean apply(MapBubble mapBubble);
    }
}
