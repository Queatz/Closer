package closer.vlllage.com.closer.handler;

import android.content.Intent;
import android.graphics.Rect;
import android.view.View;

import closer.vlllage.com.closer.GroupActivity;
import closer.vlllage.com.closer.pool.PoolMember;

import static closer.vlllage.com.closer.GroupActivity.EXTRA_GROUP_ID;

public class GroupActivityTransitionHandler extends PoolMember {
    public void showGroupMessages(View view, String groupId) {
        Intent intent = new Intent($(ActivityHandler.class).getActivity(), GroupActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        Rect bounds = new Rect();
        view.getGlobalVisibleRect(bounds);

        // Offset for status bar
        final int[] location = new int[2];
        view.getRootView().findViewById(android.R.id.content).getLocationInWindow(location);
        int windowTopOffset = location[1];
        bounds.offset(0, -windowTopOffset);

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setSourceBounds(bounds);
        $(ActivityHandler.class).getActivity().startActivity(intent);
    }
}
