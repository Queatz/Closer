package closer.vlllage.com.closer.handler.group

import android.content.Intent
import android.graphics.Rect
import android.view.View
import closer.vlllage.com.closer.GroupActivity
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_GROUP_ID
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_RESPOND
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.models.Event

class GroupActivityTransitionHandler : PoolMember() {

    @JvmOverloads
    fun showGroupMessages(view: View?, groupId: String?, isRespond: Boolean = false) {
        if (groupId == null) {
            `$`(DefaultAlerts::class.java).thatDidntWork()
            return
        }

        val intent = getIntent(groupId, isRespond)

        if (view != null) {
            val bounds = Rect()
            view.getGlobalVisibleRect(bounds)

            intent.sourceBounds = bounds
        }

        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }

    fun getIntent(groupId: String, isRespond: Boolean): Intent {
        val intent = Intent(`$`(ApplicationHandler::class.java).app, GroupActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_GROUP_ID, groupId)

        if (isRespond) {
            intent.putExtra(EXTRA_RESPOND, true)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        return intent
    }

    fun showGroupForEvent(view: View?, event: Event) {
        if (event.groupId != null) {
            `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(view, event.groupId)
        } else {
            `$`(DefaultAlerts::class.java).thatDidntWork()
        }
    }
}
