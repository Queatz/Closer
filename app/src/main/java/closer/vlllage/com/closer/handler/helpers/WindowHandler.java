package closer.vlllage.com.closer.handler.helpers;

import closer.vlllage.com.closer.pool.PoolMember;

public class WindowHandler extends PoolMember {
    public int getStatusBarHeight() {
        int statusBarHeightResId = $(ResourcesHandler.class).getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (statusBarHeightResId == 0) {
            return 0;
        }

        return $(ResourcesHandler.class).getResources().getDimensionPixelSize(statusBarHeightResId);
    }
}
