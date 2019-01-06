package closer.vlllage.com.closer.handler.helpers;

import android.content.Intent;

import closer.vlllage.com.closer.PhoneListActivity;
import closer.vlllage.com.closer.pool.PoolMember;

import static closer.vlllage.com.closer.ShareActivity.EXTRA_GROUP_MESSAGE_ID;

public class PhoneListActivityTransitionHandler extends PoolMember {
    public void showReactions(String groupMessageId) {
        Intent intent = new Intent($(ApplicationHandler.class).getApp(), PhoneListActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_GROUP_MESSAGE_ID, groupMessageId);
        $(ActivityHandler.class).getActivity().startActivity(intent);
    }
}
