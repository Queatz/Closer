package closer.vlllage.com.closer.handler.phone

import android.view.View
import closer.vlllage.com.closer.MapsActivity
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.queatz.on.On

class NavigationHandler constructor(private val on: On) {

    fun showMyProfile(view: View? = null) {
        showProfile(on<PersistenceHandler>().phoneId!!, view)
    }

    fun showProfile(phoneId: String, view: View? = null, meet: Boolean = false, close: Boolean = false) {
        val runnable = { on<GroupActivityTransitionHandler>().showGroupForPhone(view, phoneId, meet) }

        if (close && on<ActivityHandler>().activity is CircularRevealActivity) {
            (on<ActivityHandler>().activity as CircularRevealActivity).finish(runnable)
        } else {
            runnable.invoke()
        }
    }

    fun showGroup(groupId: String, view: View? = null, close: Boolean = false) {
        val runnable = { on<GroupActivityTransitionHandler>().showGroupMessages(view, groupId) }

        if (close && on<ActivityHandler>().activity is CircularRevealActivity) {
            (on<ActivityHandler>().activity as CircularRevealActivity).finish(runnable)
        } else {
            runnable.invoke()
        }
    }

    fun showMap(message: String? = null, close: Boolean = true) {
        val runnable = { on<MapActivityHandler>().goToScreen(MapsActivity.EXTRA_SCREEN_MAP, message) }

        if (close && on<ActivityHandler>().activity is CircularRevealActivity) {
            (on<ActivityHandler>().activity as CircularRevealActivity).finish(runnable)
        } else {
            runnable.invoke()
        }
    }
}
