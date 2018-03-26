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

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setSourceBounds(bounds);
        $(ActivityHandler.class).getActivity().startActivity(intent);
    }
}
