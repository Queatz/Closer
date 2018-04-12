package closer.vlllage.com.closer.handler;

import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.view.View;

import closer.vlllage.com.closer.SearchActivity;
import closer.vlllage.com.closer.pool.PoolMember;

public class SearchActivityHandler extends PoolMember {
    public void show(@Nullable View view) {
        Intent intent = new Intent($(ActivityHandler.class).getActivity(), SearchActivity.class);

        if (view != null) {
            Rect bounds = new Rect();
            view.getGlobalVisibleRect(bounds);

            // Offset for status bar
            final int[] location = new int[2];
            view.getRootView().findViewById(android.R.id.content).getLocationInWindow(location);
            int windowTopOffset = location[1];
            bounds.offset(0, -windowTopOffset);

            intent.setSourceBounds(bounds);
        }

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }
}
