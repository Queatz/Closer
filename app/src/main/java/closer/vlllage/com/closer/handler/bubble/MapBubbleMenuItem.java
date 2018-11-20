package closer.vlllage.com.closer.handler.bubble;

import android.support.annotation.DrawableRes;

public class MapBubbleMenuItem {
    private String title;
    private @DrawableRes int iconRes;

    public String getTitle() {
        return title;
    }

    public MapBubbleMenuItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getIconRes() {
        return iconRes;
    }

    public MapBubbleMenuItem setIconRes(int iconRes) {
        this.iconRes = iconRes;
        return this;
    }
}
