package closer.vlllage.com.closer;

import android.animation.Animator;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import closer.vlllage.com.closer.pool.PoolActivity;

public abstract class CircularRevealActivity extends PoolActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getIntent().getSourceBounds() != null) {

            ViewTreeObserver viewTreeObserver = getWindow().getDecorView().getRootView().getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect sourceBounds = CircularRevealActivity.this.getIntent().getSourceBounds();
                    Animator animator = ViewAnimationUtils.createCircularReveal(CircularRevealActivity.this.findViewById(CircularRevealActivity.this.getBackgroundId()),
                            sourceBounds.centerX(),
                            sourceBounds.centerY(),
                            0,
                            (float) Math.hypot(CircularRevealActivity.this.getWindow().getDecorView().getWidth(), CircularRevealActivity.this.getWindow().getDecorView().getHeight())
                    );
                    animator.setDuration(350);
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.start();

                    if (viewTreeObserver.isAlive()) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        findViewById(getBackgroundId()).setOnTouchListener((view, motionEvent) -> {
            findViewById(getBackgroundId()).setOnTouchListener(null);
            finish();
            return true;
        });
    }

    protected abstract int getBackgroundId();

    @Override
    public void finish() {
        Rect sourceBounds = getIntent().getSourceBounds();

        if (sourceBounds == null) {
            super.finish();
            return;
        }

        Animator animator = ViewAnimationUtils.createCircularReveal(findViewById(R.id.background),
                sourceBounds.centerX(),
                sourceBounds.centerY(),
                (float) Math.hypot(getWindow().getDecorView().getWidth(), getWindow().getDecorView().getHeight()),
                0
        );
        animator.setDuration(225);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                CircularRevealActivity.super.finish();
                getWindow().getDecorView().setVisibility(View.GONE);
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }
}
