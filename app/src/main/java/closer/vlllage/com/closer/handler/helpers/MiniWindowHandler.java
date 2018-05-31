package closer.vlllage.com.closer.handler.helpers;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

import static java.lang.Math.abs;

public class MiniWindowHandler extends PoolMember {
    public void attach(View toggleView, View windowView) {
        toggleView.setOnClickListener(view -> {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) windowView.getLayoutParams();
            int miniWindowHeight = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.miniWindowHeight);
            int miniWindowTopMargin = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.miniWindowTopMargin);
            if (abs(miniWindowHeight - params.matchConstraintMaxHeight) < 10) {
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
                        params.topMargin = (int) mix(startTopMargin, 0, interpolatedTime);
                        windowView.setLayoutParams(params);
                    }
                };

                animation.setDuration(225);
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
                windowView.startAnimation(animation);
            } else {
                int startMaxHeight = params.matchConstraintMaxHeight;
                int startTopMargin = params.topMargin;

                Animation animation = new Animation() {
                    @Override
                    public boolean willChangeBounds() {
                        return true;
                    }

                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        params.matchConstraintMaxHeight = (int) mix(startMaxHeight, miniWindowHeight, interpolatedTime);
                        params.topMargin = (int) mix(startTopMargin, miniWindowTopMargin, interpolatedTime);
                        windowView.setLayoutParams(params);
                    }
                };

                animation.setDuration(225);
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
                windowView.startAnimation(animation);
            }
        });
    }

    private float mix(float a, float b, float v) {
        return (a * (1 - v)) + b * v;
    }
}
