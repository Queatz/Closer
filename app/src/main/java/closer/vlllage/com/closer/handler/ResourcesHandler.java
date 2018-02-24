package closer.vlllage.com.closer.handler;

import android.content.res.Resources;

import closer.vlllage.com.closer.pool.PoolMember;

public class ResourcesHandler extends PoolMember {
    public Resources getResources() {
        return $(ActivityHandler.class).getActivity().getResources();
    }
}
