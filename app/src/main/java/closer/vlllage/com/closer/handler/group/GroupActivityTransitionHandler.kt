package closer.vlllage.com.closer.handler.group

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.view.View
import closer.vlllage.com.closer.GroupActivity
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_GROUP_ID
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_MEET
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_NEW_MEMBER
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_RESPOND
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.store.models.Event
import com.queatz.on.On

class GroupActivityTransitionHandler constructor(private val on: On) {

    @JvmOverloads
    fun showGroupMessages(view: View?, groupId: String?, isRespond: Boolean = false, isMeet: Boolean = false, isNewMember: Boolean = false) {
        if (groupId == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        val intent = getIntent(groupId, isRespond, isMeet, isNewMember)

        if (view != null) {
            val bounds = Rect()
            view.getGlobalVisibleRect(bounds)

            intent.sourceBounds = bounds
        }

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun getIntent(groupId: String, isRespond: Boolean, isMeet: Boolean = false, isNewMember: Boolean = false): Intent {
        val intent = Intent(on<ApplicationHandler>().app, GroupActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_GROUP_ID, groupId)

        if (isRespond) {
            intent.putExtra(EXTRA_RESPOND, true)
        }

        if (isMeet) {
            intent.putExtra(EXTRA_MEET, true)
        }

        if (isNewMember) {
            intent.putExtra(EXTRA_NEW_MEMBER, true)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        return intent
    }

    fun showGroupForEvent(view: View?, event: Event) {
        if (event.groupId != null) {
            on<GroupActivityTransitionHandler>().showGroupMessages(view, event.groupId)
        } else {
            on<DefaultAlerts>().thatDidntWork()
        }
    }

    @SuppressLint("CheckResult")
    fun showGroupForPhone(view: View?, phoneId: String, meet: Boolean = false) {
        on<DataHandler>().getGroupForPhone(phoneId).subscribe({
            on<GroupActivityTransitionHandler>().showGroupMessages(view, it.id, isMeet = meet)
        }, {
            on<DefaultAlerts>().thatDidntWork()
        })
    }
}
