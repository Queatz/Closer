package closer.vlllage.com.closer.handler.group

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.view.View
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.GroupActivity
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_CONTENT
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_GROUP_ID
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_MEET
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_NEW_MEMBER
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_RESPOND
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.models.Event
import com.queatz.on.On

class GroupActivityTransitionHandler constructor(private val on: On) {

    fun showGroupMessages(view: View?, groupId: String?, isRespond: Boolean = false, isMeet: Boolean = false, isNewMember: Boolean = false, isPhone: Boolean = false) {
        if (groupId == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        val intent = getIntent(groupId, isRespond, isMeet, isNewMember, isPhone)

        if (view != null) {
            val bounds = Rect()
            view.getGlobalVisibleRect(bounds)

            intent.sourceBounds = bounds
        }

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun getIntent(groupId: String, isRespond: Boolean, isMeet: Boolean = false, isNewMember: Boolean = false, isPhone: Boolean = false): Intent {
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

        if (isPhone) {
            intent.putExtra(EXTRA_CONTENT, ContentViewType.PHONE_ABOUT.name)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        return intent
    }

    fun showGroupForEvent(view: View?, event: Event) {
        if (event.groupId != null) {
            on<GroupActivityTransitionHandler>().showGroupMessages(view, event.groupId)
        } else if (event.id != null) {
            on<DisposableHandler>().add(on<DataHandler>().getEvent(event.id!!)
                    .subscribe({
                        if (it.groupId != null) {
                            on<GroupActivityTransitionHandler>().showGroupMessages(view, it.groupId)
                        } else {
                            on<DefaultAlerts>().thatDidntWork()
                        }
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
        } else {
            on<DefaultAlerts>().thatDidntWork()
        }
    }

    @SuppressLint("CheckResult")
    fun showGroupForPhone(view: View?, phoneId: String, meet: Boolean = false) {
        on<ApplicationHandler>().app.on<DisposableHandler>().add(
                on<DataHandler>().getGroupForPhone(phoneId).subscribe({
                    showGroupMessages(view, it.id, isMeet = meet, isPhone = true)
                }, {
                    on<DefaultAlerts>().thatDidntWork()
                })
        )
    }
}
