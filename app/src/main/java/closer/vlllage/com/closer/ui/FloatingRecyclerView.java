package closer.vlllage.com.closer.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import closer.vlllage.com.closer.R;

public class FloatingRecyclerView extends RecyclerView {
    private boolean isScrolling;
    private ValueAnimator colorAnimation;
    private boolean isSolidBackground;

    public FloatingRecyclerView(Context context) {
        super(context);
        init();
    }

    public FloatingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setBackgroundResource(android.R.color.transparent);
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition() > 0) {
                    if (!isSolidBackground) {
                        isSolidBackground = true;
                        animateBackground(recyclerView, R.color.dark);
                    }
                } else {
                    if (isSolidBackground) {
                        isSolidBackground = false;
                        animateBackground(recyclerView, R.color.dark_transparent);
                    }
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (!isSolidBackground && e.getAction() == MotionEvent.ACTION_DOWN) {
            setLayoutFrozen(false);
        }

        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!isSolidBackground && e.getAction() == MotionEvent.ACTION_DOWN) {
            setLayoutFrozen(!isScrolling);
        }

        return super.onTouchEvent(e);
    }

    @Override
    public void onScrollStateChanged(int state) {
        isScrolling = state != SCROLL_STATE_IDLE;
    }

    private void animateBackground(RecyclerView recyclerView, @ColorRes int color) {
//        if (true) return;

        if (colorAnimation != null) {
            colorAnimation.end();
        }

        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                ((ColorDrawable) recyclerView.getBackground()).getColor(),
                getResources().getColor(color));
        colorAnimation.setDuration(225);
        colorAnimation.addUpdateListener(animator -> setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }
}
