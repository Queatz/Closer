package closer.vlllage.com.closer.handler.group;

import android.support.annotation.DrawableRes;
import android.view.View;

import closer.vlllage.com.closer.R;

public class GroupActionBarButton {

    private String name;
    private View.OnClickListener onClick;
    private View.OnClickListener onLongClick;
    private @DrawableRes int backgroundDrawableRes;
    private @DrawableRes int icon;

    public GroupActionBarButton(String name, View.OnClickListener onClick) {
        this(name, onClick, null, R.drawable.clickable_accent);
    }

    public GroupActionBarButton(String name, View.OnClickListener onClick, View.OnClickListener onLongClick, @DrawableRes int backgroundDrawableRes) {
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

    public View.OnClickListener getOnClick() {
        return onClick;
    }

    public GroupActionBarButton setOnClick(View.OnClickListener onClick) {
        this.onClick = onClick;
        return this;
    }

    public View.OnClickListener getOnLongClick() {
        return onLongClick;
    }

    public GroupActionBarButton setOnLongClick(View.OnClickListener onLongClick) {
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
