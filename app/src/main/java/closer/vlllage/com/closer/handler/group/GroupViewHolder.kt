package closer.vlllage.com.closer.handler.group

import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout

class GroupViewHolder(rootView: View) {
    val peopleInGroup: TextView
    val groupName: TextView
    val groupAbout: TextView
    val groupDetails: TextView
    val eventToolbar: View
    val actionFrameLayout: MaxSizeFrameLayout
    val mentionSuggestionsLayout: MaxSizeFrameLayout
    val replyMessage: EditText
    val messagesRecyclerView: RecyclerView
    val pinnedMessagesRecyclerView: RecyclerView
    val shareWithRecyclerView: RecyclerView
    val searchContacts: EditText
    val contactsRecyclerView: RecyclerView
    val showPhoneContactsButton: Button
    val sendButton: ImageButton
    val sendMoreButton: ImageButton
    val scopeIndicatorButton: ImageButton
    val messagesLayoutGroup: Group
    val membersLayoutGroup: Group
    val backgroundPhoto: ImageView
    val profilePhoto: ImageView

    init {
        replyMessage = rootView.findViewById(R.id.replyMessage)
        sendMoreButton = rootView.findViewById(R.id.sendMoreButton)
        messagesRecyclerView = rootView.findViewById(R.id.messagesRecyclerView)
        pinnedMessagesRecyclerView = rootView.findViewById(R.id.pinnedMessagesRecyclerView)
        searchContacts = rootView.findViewById(R.id.searchContacts)
        contactsRecyclerView = rootView.findViewById(R.id.contactsRecyclerView)
        shareWithRecyclerView = rootView.findViewById(R.id.shareWithRecyclerView)
        peopleInGroup = rootView.findViewById(R.id.peopleInGroup)
        groupAbout = rootView.findViewById(R.id.groupAbout)
        groupDetails = rootView.findViewById(R.id.groupDetails)
        groupName = rootView.findViewById(R.id.groupName)
        eventToolbar = rootView.findViewById(R.id.eventToolbar)
        showPhoneContactsButton = rootView.findViewById(R.id.showPhoneContactsButton)
        sendButton = rootView.findViewById(R.id.sendButton)
        actionFrameLayout = rootView.findViewById(R.id.actionFrameLayout)
        mentionSuggestionsLayout = rootView.findViewById(R.id.mentionSuggestionsLayout)
        scopeIndicatorButton = rootView.findViewById(R.id.scopeIndicatorButton)
        messagesLayoutGroup = rootView.findViewById(R.id.messagesLayoutGroup)
        membersLayoutGroup = rootView.findViewById(R.id.membersLayoutGroup)
        backgroundPhoto = rootView.findViewById(R.id.backgroundPhoto)
        profilePhoto = rootView.findViewById(R.id.profilePhoto)
    }

}
