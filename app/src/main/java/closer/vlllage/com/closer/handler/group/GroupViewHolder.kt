package closer.vlllage.com.closer.handler.group

import android.view.View
import kotlinx.android.synthetic.main.activity_group.view.*

class GroupViewHolder(rootView: View) {
    val peopleInGroup = rootView.peopleInGroup!!
    val groupName = rootView.groupName!!
    val groupAbout = rootView.groupAbout!!
    val groupDetails = rootView.groupDetails!!
    val eventToolbar = rootView.eventToolbar!!
    val scopeIndicatorButton = rootView.scopeIndicatorButton!!
    val backgroundPhoto = rootView.backgroundPhoto!!
    val profilePhoto = rootView.profilePhoto!!
    val settingsButton = rootView.settingsButton!!
    val closeButton = rootView.closeButton!!
    val notificationSettingsButton = rootView.notificationSettingsButton!!
    val backgroundColor = rootView.backgroundColor!!
}
