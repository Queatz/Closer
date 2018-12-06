package closer.vlllage.com.closer.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import static java.lang.Math.max;

public class RevealAnimator {
    private ValueAnimator animator;
    private int initialHeight;
    private MaxSizeFrameLayout container;

    public RevealAnimator(@NonNull MaxSizeFrameLayout container, int initialHeight) {
        this.initialHeight = initialHeight;
        this.container = container;
    }


    public void cancel() {
        if (animator != null) {
            animator.cancel();
        }
    }

    public void show(boolean show) {
        show(show, true);
    }

    public void show(boolean show, boolean immediate) {
        if (animator != null) {
            animator.cancel();
        }

        if (!container.isAttachedToWindow()) {
            return;
        }

        if (show) {
            animator = ValueAnimator.ofInt(0, initialHeight);
            animator.setDuration(500);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setStartDelay(immediate ? 0 : 1700);
            animator.addUpdateListener(animation -> {
                container.setMaxHeight((int) animation.getAnimatedValue());
                container.setAlpha(animation.getAnimatedFraction());
            });
            animator.addListener(new Animator.AnimatorListener() {

                private boolean cancelled;

                @Override
                public void onAnimationStart(Animator animation) {
                    container.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (cancelled) {
                        return;
                    }
                    container.setMaxHeight(MaxSizeFrameLayout.UNSPECIFIED);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    cancelled = true;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.start();
        } else if (container.getVisibility() != View.GONE) {
            initialHeight = max(initialHeight, container.getMeasuredHeight());
            animator = ValueAnimator.ofInt(container.getMeasuredHeight(), 0);
            animator.setDuration(195);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                container.setMaxHeight((int) animation.getAnimatedValue());
                container.setAlpha(1 - animation.getAnimatedFraction());
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    container.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    container.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.start();
        }
    }
}
