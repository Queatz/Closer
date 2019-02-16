package closer.vlllage.com.closer.handler.share;

import android.content.Intent;

import closer.vlllage.com.closer.ShareActivity;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;

import static closer.vlllage.com.closer.ShareActivity.EXTRA_GROUP_MESSAGE_ID;
import static closer.vlllage.com.closer.ShareActivity.EXTRA_INVITE_TO_GROUP_PHONE_ID;
import static closer.vlllage.com.closer.ShareActivity.EXTRA_SHARE_GROUP_TO_GROUP_ID;

public class ShareActivityTransitionHandler extends PoolMember {
    public void shareGroupMessage(String groupMessageId) {
        Intent intent = new Intent($(ApplicationHandler.class).getApp(), ShareActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_GROUP_MESSAGE_ID, groupMessageId);

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }

    public void inviteToGroup(String phoneId) {
        Intent intent = new Intent($(ApplicationHandler.class).getApp(), ShareActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_INVITE_TO_GROUP_PHONE_ID, phoneId);

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }
    public void shareGroupToGroup(String groupId) {
        Intent intent = new Intent($(ApplicationHandler.class).getApp(), ShareActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_SHARE_GROUP_TO_GROUP_ID, groupId);

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }
}
