package closer.vlllage.com.closer.handler.group;

import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.view.View;

import closer.vlllage.com.closer.GroupActivity;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;

import static closer.vlllage.com.closer.GroupActivity.EXTRA_GROUP_ID;
import static closer.vlllage.com.closer.GroupActivity.EXTRA_RESPOND;

public class GroupActivityTransitionHandler extends PoolMember {
    public void showGroupMessages(@Nullable View view, String groupId) {
        showGroupMessages(view, groupId, false);
    }

    public void showGroupMessages(@Nullable View view, String groupId, boolean isRespond) {
        Intent intent = getIntent(groupId, isRespond);

        if (view != null) {
            Rect bounds = new Rect();
            view.getGlobalVisibleRect(bounds);

            intent.setSourceBounds(bounds);
        }

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }

    public Intent getIntent(String groupId, boolean isRespond) {
        Intent intent = new Intent($(ApplicationHandler.class).getApp(), GroupActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_GROUP_ID, groupId);

        if (isRespond) {
            intent.putExtra(EXTRA_RESPOND, true);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
}
