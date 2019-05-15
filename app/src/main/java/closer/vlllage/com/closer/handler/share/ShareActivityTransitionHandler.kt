package closer.vlllage.com.closer.handler.share

import android.content.Intent
import closer.vlllage.com.closer.ShareActivity
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_GROUP_MESSAGE_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_INVITE_TO_GROUP_PHONE_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_SHARE_GROUP_TO_GROUP_ID
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On

class ShareActivityTransitionHandler constructor(private val on: On) {
    fun shareGroupMessage(groupMessageId: String) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_GROUP_MESSAGE_ID, groupMessageId)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun inviteToGroup(phoneId: String) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_INVITE_TO_GROUP_PHONE_ID, phoneId)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun shareGroupToGroup(groupId: String) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_SHARE_GROUP_TO_GROUP_ID, groupId)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }
}
