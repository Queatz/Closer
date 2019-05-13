package closer.vlllage.com.closer.handler.helpers

import android.content.Intent
import closer.vlllage.com.closer.PhoneListActivity
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_GROUP_MESSAGE_ID
import closer.vlllage.com.closer.pool.PoolMember

class PhoneListActivityTransitionHandler : PoolMember() {
    fun showReactions(groupMessageId: String) {
        val intent = Intent(`$`(ApplicationHandler::class.java).app, PhoneListActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_GROUP_MESSAGE_ID, groupMessageId)
        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }
}
