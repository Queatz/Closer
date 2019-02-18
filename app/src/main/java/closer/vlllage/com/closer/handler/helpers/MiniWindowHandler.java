package closer.vlllage.com.closer.handler.helpers;

import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.settings.SettingsHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.ui.DragTriggerView;
import closer.vlllage.com.closer.ui.TimedValue;

import static closer.vlllage.com.closer.handler.settings.UserLocalSetting.CLOSER_SETTINGS_OPEN_GROUP_EXPANDED;
import static java.lang.Math.abs;
import static java.lang.Math.max;

public class MiniWindowHandler extends PoolMember {

    private static final int CLOSE_TUG_SLOP = 32;

    public void attach(View toggleView, View windowView, @Nullable MiniWindowEventListener miniWindowEventListener) {

        final int miniWindowHeight = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.miniWindowHeight);
        final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) windowView.getLayoutParams();
        final int miniWindowMinTopMargin = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.miniWindowMinTopMargin);
        final int miniWindowTopMargin = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.miniWindowTopMargin);

        windowView.setClipToOutline(true);

        toggleView.setClickable(true);
        new DragTriggerView(toggleView, new DragTriggerView.OnDragEventListener() {

            private boolean dead;

            private int startMaxHeight = 0;
            private double startY = 0;

            @Override
            public void onDragStart(TimedValue<Float> x, TimedValue<Float> y) {
                if (dead) return;

                startMaxHeight = windowView.getMeasuredHeight();
                params.topMargin = miniWindowMinTopMargin;
                windowView.setLayoutParams(params);
                startY = y.get();
            }

            @Override
            public void onDragRelease(TimedValue<Float> x, TimedValue<Float> y) {
                if (dead) return;

                int startMaxHeight = params.matchConstraintMaxHeight;
                int startTopMargin = params.topMargin;

                double yVelocity = y.now() - y.get();

                if (abs(yVelocity) < 10) {
                    return;
                }

                Animation animation = new Animation() {
                    @Override
                    public boolean willChangeBounds() {
                        return true;
                    }

                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        if (yVelocity > 0) {
                            params.matchConstraintMaxHeight = (int) mix(startMaxHeight, miniWindowHeight, interpolatedTime);
                            params.topMargin = (int) mix(startTopMargin, miniWindowTopMargin, interpolatedTime);
                        } else {
                            params.matchConstraintMaxHeight = (int) mix(startMaxHeight, miniWindowHeight * 3, interpolatedTime);
                            params.topMargin = (int) mix(startTopMargin, miniWindowMinTopMargin, interpolatedTime);
                        }
                        windowView.setLayoutParams(params);
                    }
                };

                animation.setDuration(225);
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
                windowView.startAnimation(animation);
            }

            @Override
            public void onDragUpdate(TimedValue<Float> x, TimedValue<Float> y) {
                if (dead) return;

                int currentMiniWindowHeight = (int) (startMaxHeight + (startY - y.get()));

                if (startY < y.get() && currentMiniWindowHeight - miniWindowHeight < -CLOSE_TUG_SLOP && miniWindowEventListener != null) {
                    dead = true;
                    miniWindowEventListener.onMiniWindowShouldClose();
                }

                params.matchConstraintMaxHeight = max(miniWindowHeight, currentMiniWindowHeight);
                windowView.setLayoutParams(params);
            }
        });

        if ($(SettingsHandler.class).get(CLOSER_SETTINGS_OPEN_GROUP_EXPANDED)) {
            int startMaxHeight = params.matchConstraintMaxHeight;
            int startTopMargin = params.topMargin;

            Animation animation = new Animation() {
                @Override
                public boolean willChangeBounds() {
                    return true;
                }

                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    params.matchConstraintMaxHeight = (int) mix(startMaxHeight, miniWindowHeight * 3, interpolatedTime);
                    params.topMargin = (int) mix(startTopMargin, miniWindowMinTopMargin, interpolatedTime);
                    windowView.setLayoutParams(params);
                }
            };

            animation.setDuration(225);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            windowView.startAnimation(animation);
        }
    }

    private float mix(float a, float b, float v) {
        return (a * (1 - v)) + b * v;
    }

    public interface MiniWindowEventListener {
        void onMiniWindowShouldClose();
    }
}
