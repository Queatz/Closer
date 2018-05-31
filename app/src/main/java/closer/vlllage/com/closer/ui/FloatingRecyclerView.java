package closer.vlllage.com.closer.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class FloatingRecyclerView extends RecyclerView {
    private boolean isScrolling;

    public FloatingRecyclerView(Context context) {
        super(context);
    }

    public FloatingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            setLayoutFrozen(false);
        }

        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            setLayoutFrozen(!isScrolling);
        }

        return super.onTouchEvent(e);
    }

    @Override
    public void onScrollStateChanged(int state) {
        isScrolling = state != SCROLL_STATE_IDLE;
    }
}
