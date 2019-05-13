package closer.vlllage.com.closer.handler.share

import android.content.Intent
import closer.vlllage.com.closer.ShareActivity
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_GROUP_MESSAGE_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_INVITE_TO_GROUP_PHONE_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_SHARE_GROUP_TO_GROUP_ID
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.pool.PoolMember

class ShareActivityTransitionHandler : PoolMember() {
    fun shareGroupMessage(groupMessageId: String) {
        val intent = Intent(`$`(ApplicationHandler::class.java).app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_GROUP_MESSAGE_ID, groupMessageId)

        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }

    fun inviteToGroup(phoneId: String) {
        val intent = Intent(`$`(ApplicationHandler::class.java).app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_INVITE_TO_GROUP_PHONE_ID, phoneId)

        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }

    fun shareGroupToGroup(groupId: String) {
        val intent = Intent(`$`(ApplicationHandler::class.java).app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_SHARE_GROUP_TO_GROUP_ID, groupId)

        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }
}
