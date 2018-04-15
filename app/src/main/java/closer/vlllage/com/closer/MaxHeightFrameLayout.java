package closer.vlllage.com.closer;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class MaxHeightFrameLayout extends FrameLayout {
    private int maxHeight;

    public MaxHeightFrameLayout(@NonNull Context context) {
        super(context);
    }

    public MaxHeightFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaxHeightFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MaxHeightFrameLayout,
                0, 0);

        try {
            maxHeight = typedArray.getDimensionPixelSize(R.styleable.MaxHeightFrameLayout_maxHeight, -1);
        } finally {
            typedArray.recycle();
        }
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public MaxHeightFrameLayout setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        invalidate();
        requestLayout();
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight > 0) {
            int hSize = MeasureSpec.getSize(heightMeasureSpec);
            int hMode = MeasureSpec.getMode(heightMeasureSpec);

            switch (hMode) {
                case MeasureSpec.AT_MOST:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.AT_MOST);
                    break;
                case MeasureSpec.UNSPECIFIED:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
                    break;
                case MeasureSpec.EXACTLY:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.EXACTLY);
                    break;
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
