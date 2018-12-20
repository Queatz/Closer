package closer.vlllage.com.closer.handler.share;

import android.content.Intent;

import closer.vlllage.com.closer.ShareActivity;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class ShareActivityTransitionHandler extends PoolMember {
    public void share() {
        Intent intent = new Intent($(ApplicationHandler.class).getApp(), ShareActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
//        intent.putExtra(EXTRA_GROUP_MESSAGE, groupMessageId);

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }
}
