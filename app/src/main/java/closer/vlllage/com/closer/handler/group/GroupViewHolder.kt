package closer.vlllage.com.closer.handler.group

import android.view.View
import kotlinx.android.synthetic.main.activity_group.view.*

class GroupViewHolder(rootView: View) {
    val meetLayout = rootView.meetLayout!!
    val peopleInGroup = rootView.peopleInGroup!!
    val groupName = rootView.groupName!!
    val groupAbout = rootView.groupAbout!!
    val groupRatingAverage = rootView.groupRatingAverage!!
    val groupRatingCount = rootView.groupRatingCount!!
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
