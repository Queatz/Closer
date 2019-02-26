package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout;

public class GroupViewHolder {
    public final TextView peopleInGroup;
    public final TextView groupName;
    public final TextView groupAbout;
    public final TextView groupDetails;
    public final View eventToolbar;
    public final MaxSizeFrameLayout actionFrameLayout;
    public final MaxSizeFrameLayout mentionSuggestionsLayout;
    public final EditText replyMessage;
    public final RecyclerView messagesRecyclerView;
    public final RecyclerView pinnedMessagesRecyclerView;
    public final RecyclerView shareWithRecyclerView;
    public final EditText searchContacts;
    public final RecyclerView contactsRecyclerView;
    public final Button showPhoneContactsButton;
    public final ImageButton sendButton;
    public final ImageButton sendMoreButton;
    public final ImageButton scopeIndicatorButton;
    public final android.support.constraint.Group messagesLayoutGroup;
    public final android.support.constraint.Group membersLayoutGroup;
    public final ImageView backgroundPhoto;

    public GroupViewHolder(View rootView) {
        replyMessage = rootView.findViewById(R.id.replyMessage);
        sendMoreButton = rootView.findViewById(R.id.sendMoreButton);
        messagesRecyclerView = rootView.findViewById(R.id.messagesRecyclerView);
        pinnedMessagesRecyclerView = rootView.findViewById(R.id.pinnedMessagesRecyclerView);
        searchContacts = rootView.findViewById(R.id.searchContacts);
        contactsRecyclerView = rootView.findViewById(R.id.contactsRecyclerView);
        shareWithRecyclerView = rootView.findViewById(R.id.shareWithRecyclerView);
        peopleInGroup = rootView.findViewById(R.id.peopleInGroup);
        groupAbout = rootView.findViewById(R.id.groupAbout);
        groupDetails = rootView.findViewById(R.id.groupDetails);
        groupName = rootView.findViewById(R.id.groupName);
        eventToolbar = rootView.findViewById(R.id.eventToolbar);
        showPhoneContactsButton = rootView.findViewById(R.id.showPhoneContactsButton);
        sendButton = rootView.findViewById(R.id.sendButton);
        actionFrameLayout = rootView.findViewById(R.id.actionFrameLayout);
        mentionSuggestionsLayout = rootView.findViewById(R.id.mentionSuggestionsLayout);
        scopeIndicatorButton = rootView.findViewById(R.id.scopeIndicatorButton);
        messagesLayoutGroup = rootView.findViewById(R.id.messagesLayoutGroup);
        membersLayoutGroup = rootView.findViewById(R.id.membersLayoutGroup);
        backgroundPhoto = rootView.findViewById(R.id.backgroundPhoto);
    }

}
