package closer.vlllage.com.closer.handler;

import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.view.View;

import closer.vlllage.com.closer.GroupActivity;
import closer.vlllage.com.closer.pool.PoolMember;

import static closer.vlllage.com.closer.GroupActivity.EXTRA_GROUP_ID;

public class GroupActivityTransitionHandler extends PoolMember {
    public void showGroupMessages(@Nullable View view, String groupId) {
        Intent intent = getIntent(groupId);

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

    public Intent getIntent(String groupId) {
        Intent intent = new Intent($(ApplicationHandler.class).getApp(), GroupActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
}
