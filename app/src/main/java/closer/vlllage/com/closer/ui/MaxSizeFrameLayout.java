package closer.vlllage.com.closer.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import closer.vlllage.com.closer.R;

public class MaxSizeFrameLayout extends FrameLayout {
    private int maxHeight;
    private int maxWidth;

    public MaxSizeFrameLayout(@NonNull Context context) {
        super(context);
    }

    public MaxSizeFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaxSizeFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MaxSizeFrameLayout,
                0, 0);

        try {
            maxHeight = typedArray.getDimensionPixelSize(R.styleable.MaxSizeFrameLayout_maxHeight, -1);
            maxWidth = typedArray.getDimensionPixelSize(R.styleable.MaxSizeFrameLayout_maxWidth, -1);
        } finally {
            typedArray.recycle();
        }
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public MaxSizeFrameLayout setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        invalidate();
        requestLayout();
        return this;
    }

    public MaxSizeFrameLayout setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        invalidate();
        requestLayout();
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight >= 0) {
            heightMeasureSpec = calculate(heightMeasureSpec, maxHeight);
        }

        if (maxWidth >= 0) {
            widthMeasureSpec = calculate(widthMeasureSpec, maxWidth);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int calculate(int measureSpec, int max) {
        int hSize = MeasureSpec.getSize(measureSpec);
        int hMode = MeasureSpec.getMode(measureSpec);

        switch (hMode) {
            case MeasureSpec.AT_MOST:
                return MeasureSpec.makeMeasureSpec(Math.min(hSize, max), MeasureSpec.AT_MOST);
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.makeMeasureSpec(max, MeasureSpec.AT_MOST);
            case MeasureSpec.EXACTLY:
                return MeasureSpec.makeMeasureSpec(Math.min(hSize, max), MeasureSpec.EXACTLY);
            default:
                return measureSpec;
        }
    }
}
