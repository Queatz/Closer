package closer.vlllage.com.closer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class InterceptableScrollView extends ScrollView {

    private OnTouchListener onInterceptTouchListener;

    public InterceptableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InterceptableScrollView(Context context) {
        super(context);
    }

    public void setOnInterceptTouchListener(OnTouchListener onInterceptTouchListener) {
        this.onInterceptTouchListener = onInterceptTouchListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (onInterceptTouchListener != null) {
            if (onInterceptTouchListener.onTouch(this, event)) {
                return true;
            }
        }

        return super.onInterceptTouchEvent(event);
    }
}
