package closer.vlllage.com.closer.handler.group

import android.view.View
import kotlinx.android.synthetic.main.activity_group.view.*

class GroupViewHolder(rootView: View) {
    val peopleInGroup = rootView.peopleInGroup!!
    val groupName = rootView.groupName!!
    val groupAbout = rootView.groupAbout!!
    val groupDetails = rootView.groupDetails!!
    val eventToolbar = rootView.eventToolbar!!
    val actionFrameLayout = rootView.actionFrameLayout!!
    val mentionSuggestionsLayout = rootView.mentionSuggestionsLayout!!
    val mentionSuggestionRecyclerView = rootView.mentionSuggestionRecyclerView!!
    val replyMessage = rootView.replyMessage!!
    val messagesRecyclerView = rootView.messagesRecyclerView!!
    val pinnedMessagesRecyclerView = rootView.pinnedMessagesRecyclerView!!
    val shareWithRecyclerView = rootView.shareWithRecyclerView!!
    val searchContacts = rootView.searchContacts!!
    val contactsRecyclerView = rootView.contactsRecyclerView!!
    val showPhoneContactsButton = rootView.showPhoneContactsButton!!
    val sendButton = rootView.sendButton!!
    val sendMoreButton = rootView.sendMoreButton!!
    val scopeIndicatorButton = rootView.scopeIndicatorButton!!
    val messagesLayoutGroup = rootView.messagesLayoutGroup!!
    val membersLayoutGroup = rootView.membersLayoutGroup!!
    val backgroundPhoto = rootView.backgroundPhoto!!
    val profilePhoto = rootView.profilePhoto!!
    val settingsButton = rootView.settingsButton!!
    val closeButton = rootView.closeButton!!
    val notificationSettingsButton = rootView.notificationSettingsButton!!
    val actionRecyclerView = rootView.actionRecyclerView!!
    val sendMoreLayout = rootView.sendMoreLayout!!
    val backgroundColor = rootView.backgroundColor!!
}
