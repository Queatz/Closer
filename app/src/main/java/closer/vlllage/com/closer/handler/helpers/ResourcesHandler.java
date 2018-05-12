package closer.vlllage.com.closer.handler.helpers;

import android.content.res.Resources;

import closer.vlllage.com.closer.pool.PoolMember;

public class ResourcesHandler extends PoolMember {
    public Resources getResources() {
        return $(ApplicationHandler.class).getApp().getResources();
    }
}
