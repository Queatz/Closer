package closer.vlllage.com.closer.handler.phone

import android.view.View
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.queatz.on.On

class ProfileHandler constructor(private val on: On) {
    fun showProfile(phoneId: String, view: View? = null) {
        val runnable = { on<GroupActivityTransitionHandler>().showGroupForPhone(view, phoneId) }

        if (on<ActivityHandler>().activity is CircularRevealActivity) {
            (on<ActivityHandler>().activity as CircularRevealActivity).finish(runnable)
        } else {
            runnable.invoke()
        }
    }
}
