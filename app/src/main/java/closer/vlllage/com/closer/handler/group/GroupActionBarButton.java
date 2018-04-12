package closer.vlllage.com.closer.handler.group;

import android.support.annotation.DrawableRes;

import closer.vlllage.com.closer.R;

public class GroupActionBarButton {

    private String name;
    private Runnable onClick;
    private Runnable onLongClick;
    private @DrawableRes int backgroundDrawableRes;
    private @DrawableRes int icon;

    public GroupActionBarButton(String name, Runnable onClick) {
        this(name, onClick, null, R.drawable.clickable_accent);
    }

    public GroupActionBarButton(String name, Runnable onClick, Runnable onLongClick, @DrawableRes int backgroundDrawableRes) {
        this.name = name;
        this.onClick = onClick;
        this.onLongClick = onLongClick;
        this.backgroundDrawableRes = backgroundDrawableRes;
    }

    public String getName() {
        return name;
    }

    public GroupActionBarButton setName(String name) {
        this.name = name;
        return this;
    }

    public Runnable getOnClick() {
        return onClick;
    }

    public GroupActionBarButton setOnClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    public Runnable getOnLongClick() {
        return onLongClick;
    }

    public GroupActionBarButton setOnLongClick(Runnable onLongClick) {
        this.onLongClick = onLongClick;
        return this;
    }

    public int getBackgroundDrawableRes() {
        return backgroundDrawableRes;
    }

    public GroupActionBarButton setBackgroundDrawableRes(int backgroundDrawableRes) {
        this.backgroundDrawableRes = backgroundDrawableRes;
        return this;
    }

    public int getIcon() {
        return icon;
    }

    public GroupActionBarButton setIcon(int icon) {
        this.icon = icon;
        return this;
    }
}
