package closer.vlllage.com.closer.ui;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.WindowInsets;

public class FitsSystemWindowsConstraintLayout extends ConstraintLayout {
    public FitsSystemWindowsConstraintLayout(Context context) {
        super(context);
        init();
    }

    public FitsSystemWindowsConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FitsSystemWindowsConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFitsSystemWindows(true);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        super.onApplyWindowInsets(insets);
        return insets;
    }
}
