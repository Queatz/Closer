package closer.vlllage.com.closer.ui;

import android.animation.Animator;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;

import closer.vlllage.com.closer.pool.PoolActivity;

public abstract class CircularRevealActivity extends PoolActivity {
    private Rect sourceBounds;
    private Runnable finishCallback;
    private Animator finishAnimator;
    private View background;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        background = findViewById(getBackgroundId());

        sourceBounds = getIntent().getSourceBounds();

        ViewTreeObserver viewTreeObserver = background.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                background.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                reveal();
            }
        });

        background.setOnTouchListener((view, motionEvent) -> {
            background.setOnTouchListener(null);
            finish();
            return true;
        });
    }

    protected void reveal() {
        if (sourceBounds == null) {
            Rect rect = new Rect();
            getWindow().getDecorView().getGlobalVisibleRect(rect);
            rect.top = rect.bottom;
            sourceBounds = rect;
        }

        Animator animator = ViewAnimationUtils.createCircularReveal(background,
                sourceBounds.centerX(),
                sourceBounds.centerY(),
                0,
                (float) Math.hypot(CircularRevealActivity.this.getWindow().getDecorView().getWidth(), CircularRevealActivity.this.getWindow().getDecorView().getHeight())
        );
        animator.setDuration(225);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();

        getWindow().getDecorView().setAlpha(0f);
        getWindow().getDecorView().animate()
                .alpha(1f)
                .setDuration(225)
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }

    protected void setSourceBounds(Rect sourceBounds) {
        this.sourceBounds = sourceBounds;
    }

    protected abstract int getBackgroundId();

    @Override
    public void finish() {
        if (sourceBounds == null) {
            super.finish();
            return;
        }

        if (finishAnimator != null && finishAnimator.isRunning()) {
            return;
        }

        finishAnimator = ViewAnimationUtils.createCircularReveal(findViewById(getBackgroundId()),
                sourceBounds.centerX(),
                sourceBounds.centerY(),
                (float) Math.hypot(getWindow().getDecorView().getWidth(), getWindow().getDecorView().getHeight()),
                0
        );
        finishAnimator.setDuration(195);
        finishAnimator.setInterpolator(new AccelerateInterpolator(0.5f));
        finishAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                CircularRevealActivity.super.finish();
                getWindow().getDecorView().setVisibility(View.GONE);
                overridePendingTransition(0, 0);

                if (finishCallback != null) {
                    finishCallback.run();
                    finishCallback = null;
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                CircularRevealActivity.super.finish();
                getWindow().getDecorView().setVisibility(View.GONE);
                overridePendingTransition(0, 0);

                if (finishCallback != null) {
                    finishCallback.run();
                    finishCallback = null;
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        finishAnimator.start();

        getWindow().getDecorView().animate()
                .alpha(0f)
                .setDuration(195)
                .setInterpolator(new AccelerateInterpolator(0.5f))
                .start();
    }

    public void finish(Runnable callback) {
        finishCallback = callback;
        finish();
    }
}
