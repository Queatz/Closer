package closer.vlllage.com.closer.handler.helpers

import android.content.Intent
import closer.vlllage.com.closer.PhoneListActivity
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_GOAL_NAME
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_GROUP_MESSAGE_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_LIFESTYLE_NAME
import com.queatz.on.On

class PhoneListActivityTransitionHandler constructor(private val on: On) {
    fun showReactions(groupMessageId: String) {
        val intent = Intent(on<ApplicationHandler>().app, PhoneListActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_GROUP_MESSAGE_ID, groupMessageId)
        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun showPhonesForGoal(goalName: String) {
        val intent = Intent(on<ApplicationHandler>().app, PhoneListActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_GOAL_NAME, goalName)
        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun showPhonesForLifestyle(goalName: String) {
        val intent = Intent(on<ApplicationHandler>().app, PhoneListActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_LIFESTYLE_NAME, goalName)
        on<ActivityHandler>().activity!!.startActivity(intent)
    }
}
