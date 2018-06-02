package closer.vlllage.com.closer.ui;

import android.animation.Animator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;

import java.util.Date;

import closer.vlllage.com.closer.R;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;

public class DraggableView {

    private static final int SINGLE_TAP_CONFIRM_TIME_MS = 100;
    private static final int SINGLE_TAP_CONFIRM_MAX_VELOCITY = 2;

    private Float positionBeforeKeyboardOpenedX = null;
    private Float positionBeforeKeyboardOpenedY = null;
    private final View view;
    private final View container;
    private boolean moveToBottom;
    private Date dragStartTime = new Date();
    private ViewPropertyAnimator centerAnimation;

    public DraggableView(final View view, final View container) {
        this.view = view;
        this.container = container;

        view.setClickable(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            private float xDiffInTouchPointAndViewTopLeftCorner;
            private float yDiffInTouchPointAndViewTopLeftCorner;

            private TimedValue<Float> trackedX = new TimedValue<>(75);
            private TimedValue<Float> trackedY = new TimedValue<>(75);

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.clearAnimation();
                        xDiffInTouchPointAndViewTopLeftCorner = motionEvent.getRawX() - view.getX();
                        yDiffInTouchPointAndViewTopLeftCorner = motionEvent.getRawY() - view.getY();
                        dragStartTime = new Date();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.setX(clampX(motionEvent.getRawX() - xDiffInTouchPointAndViewTopLeftCorner));
                        view.setY(clampY(motionEvent.getRawY() - yDiffInTouchPointAndViewTopLeftCorner));
                        trackedX.report(motionEvent.getRawX());
                        trackedY.report(motionEvent.getRawY());
                        positionBeforeKeyboardOpenedX = null;
                        positionBeforeKeyboardOpenedY = null;
                        break;
                    case MotionEvent.ACTION_UP:
                        Float fromX = trackedX.get();
                        Float fromY = trackedY.get();

                        if (fromX != null && fromY != null) {
                            double velocity = hypot(motionEvent.getRawX() - fromX, motionEvent.getRawY() - fromY);

                            if (velocity > SINGLE_TAP_CONFIRM_MAX_VELOCITY) {
                                double angle = atan2(motionEvent.getRawY() - fromY, motionEvent.getRawX() - fromX);
                                animate(clampXForAnimation(view.getX() + 5 * velocity * cos(angle)), clampY(view.getY() + velocity * sin(angle)));
                            } else if (isSingleTap()) {
                                center();
                            }
                        } else if (isSingleTap()) {
                            center();
                        }
                        break;
                }

                return true;
            }

            private boolean isSingleTap() {
                return new Date().getTime() - dragStartTime.getTime() < SINGLE_TAP_CONFIRM_TIME_MS;
            }
        });

        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private int previousHeight = 0;

            @Override
            public void onGlobalLayout() {
                if (moveToBottom) {
                    moveToBottom = false;
                    view.setX(clampX(container.getWidth()));
                    view.setY(clampY(container.getHeight() / 3 - view.getHeight() / 2));
                    return;
                }

                int h = container.getHeight() - container.getPaddingBottom();

                if (view.findFocus() != null && previousHeight > h) {
                    if (positionBeforeKeyboardOpenedX == null && positionBeforeKeyboardOpenedY == null) {
                        positionBeforeKeyboardOpenedX = view.getX();
                        positionBeforeKeyboardOpenedY = view.getY();
                    }
                    animate(container.getWidth() / 2 - view.getWidth() / 2, h - view.getHeight() - view.getPaddingBottom());
                } else if (previousHeight <= h && positionBeforeKeyboardOpenedX != null && positionBeforeKeyboardOpenedY != null) {
                    animate(clampX(positionBeforeKeyboardOpenedX), clampY(positionBeforeKeyboardOpenedY));

                    positionBeforeKeyboardOpenedX = null;
                    positionBeforeKeyboardOpenedY = null;
                } else if (clampX(view.getX()) != view.getX() || clampY(view.getX()) != view.getY()) {
                    animate(clampX(view.getX()), clampY(view.getY()));
                }

                previousHeight = container.getHeight();
            }
        });
    }

    private void animate(float x, float y) {
        if (centerAnimation != null) {
            return;
        }

        view.animate()
                .x(x)
                .y(y)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(225)
                .start();
    }

    public void center() {
        centerAnimation = view.animate()
                .x(container.getWidth() / 2 - view.getWidth() / 2)
                .y(container.getHeight() / 2 - view.getHeight() / 2)
                .setInterpolator(new OvershootInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        centerAnimation = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        centerAnimation = null;
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .setDuration(225);
        centerAnimation.start();
    }

    public void moveToBottom() {
        moveToBottom = true;
    }

    private float clampXForAnimation(double x) {
        int m = view.getContext().getResources().getDimensionPixelSize(R.dimen.padDouble) * 2;
        int w = container.getWidth();

        if (x + view.getWidth() < w / 3) {
            return -view.getWidth() + m;
        } else if (x > w / 3 * 2) {
            return w - m;
        } else {
            return w / 2 - view.getWidth() / 2;
        }
    }

    private float clampX(double x) {
        int m = view.getContext().getResources().getDimensionPixelSize(R.dimen.padDouble) * 2;
        return (float) max(-view.getWidth() + m, min(container.getWidth() - m, x));
    }

    private float clampY(double y) {
        int m = view.getContext().getResources().getDimensionPixelSize(R.dimen.padDouble) * 2;
        return (float) max(-view.getHeight() + m, min(container.getHeight() - m, y));
    }
}
