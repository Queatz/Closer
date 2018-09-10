package closer.vlllage.com.closer.handler.helpers;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.ui.DragTriggerView;
import closer.vlllage.com.closer.ui.TimedValue;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class MiniWindowHandler extends PoolMember {
    public void attach(View toggleView, View windowView) {
        windowView.setClipToOutline(true);

        toggleView.setClickable(true);
        new DragTriggerView(toggleView, new DragTriggerView.OnDragEventListener() {

            private int startMaxHeight = 0;
            private double startY = 0;
            private int miniWindowHeight = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.miniWindowHeight);
            private ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) windowView.getLayoutParams();
            private int miniWindowMinTopMargin = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.pad);
            private int miniWindowTopMargin = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.miniWindowTopMargin);

            @Override
            public void onDragStart(TimedValue<Float> x, TimedValue<Float> y) {
                startMaxHeight = windowView.getMeasuredHeight();
                params.topMargin = miniWindowMinTopMargin;
                windowView.setLayoutParams(params);
                startY = y.get();
            }

            @Override
            public void onDragRelease(TimedValue<Float> x, TimedValue<Float> y) {
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
                params.matchConstraintMaxHeight = max(miniWindowHeight, (int) (startMaxHeight + (startY - y.get())));
                windowView.setLayoutParams(params);
            }
        });
    }

    private float mix(float a, float b, float v) {
        return (a * (1 - v)) + b * v;
    }
}
