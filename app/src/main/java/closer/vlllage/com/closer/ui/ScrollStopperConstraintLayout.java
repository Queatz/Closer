package closer.vlllage.com.closer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ScrollStopperConstraintLayout extends ConstraintLayout {
    public ScrollStopperConstraintLayout(Context context) {
        super(context);
    }

    public ScrollStopperConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollStopperConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        return false;
    }
}
