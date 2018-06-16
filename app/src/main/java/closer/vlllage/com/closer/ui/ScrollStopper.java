package closer.vlllage.com.closer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by jacob on 2/25/15.
 */
public class ScrollStopper extends FrameLayout {

    public ScrollStopper(Context context) {
        super(context);
    }

    public ScrollStopper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollStopper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        return false;
    }
}
