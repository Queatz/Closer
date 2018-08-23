package closer.vlllage.com.closer.ui;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import static java.lang.Math.abs;

public class SoftScrollStopper extends FrameLayout {

    private static final int SLOP_RADIUS = 16;

    private boolean isChildScrolling;
    private Point originPosition = new Point();

    public SoftScrollStopper(@NonNull Context context) {
        super(context);
    }

    public SoftScrollStopper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SoftScrollStopper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                isChildScrolling = false;
                break;
            case MotionEvent.ACTION_DOWN:
                originPosition.x = (int) event.getRawX();
                originPosition.y = (int) event.getRawY();
                isChildScrolling = true;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (event.getRawX() - originPosition.x);
                int deltaY = (int) (event.getRawY() - originPosition.y);

                if (abs(deltaX) > SLOP_RADIUS || abs(deltaY) > SLOP_RADIUS) {
                    if (abs(deltaX) > abs(deltaY)) {
                        if (deltaX > 0 && getChildAt(0).canScrollHorizontally(-1)) {
                            isChildScrolling = true;
                        } else if (deltaX < 0 && getChildAt(0).canScrollHorizontally(1)) {
                            isChildScrolling = true;
                        } else {
                            isChildScrolling = false;
                        }
                    } else {
                        isChildScrolling = false;
                    }
                }

                break;
        }

        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(isChildScrolling);
        }

        return false;
    }
}
