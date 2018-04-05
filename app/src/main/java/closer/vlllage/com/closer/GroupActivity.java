package closer.vlllage.com.closer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import closer.vlllage.com.closer.handler.AccountHandler;
import closer.vlllage.com.closer.handler.ApiHandler;
import closer.vlllage.com.closer.handler.PermissionHandler;
import closer.vlllage.com.closer.handler.group.GroupContactsHandler;
import closer.vlllage.com.closer.handler.group.GroupHandler;
import closer.vlllage.com.closer.handler.group.GroupMessagesHandler;

import static android.Manifest.permission.READ_CONTACTS;

public class GroupActivity extends CircularRevealActivity {

    public static final String EXTRA_GROUP_ID = "groupId";
    private TextView peopleInGroup;
    private TextView groupName;
    private EditText replyMessage;
    private RecyclerView messagesRecyclerView;
    private EditText searchContacts;
    private RecyclerView contactsRecyclerView;
    private Button showPhoneContactsButton;
    private ImageButton sendButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        replyMessage = findViewById(R.id.replyMessage);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        searchContacts = findViewById(R.id.searchContacts);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        peopleInGroup = findViewById(R.id.peopleInGroup);
        groupName = findViewById(R.id.groupName);
        showPhoneContactsButton = findViewById(R.id.showPhoneContactsButton);
        sendButton = findViewById(R.id.sendButton);

        findViewById(R.id.closeButton).setOnClickListener(view -> finish());

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        $(GroupHandler.class).attach(groupName, peopleInGroup);
        if (getIntent() != null && getIntent().hasExtra(EXTRA_GROUP_ID)) {
            $(GroupHandler.class).setGroupById(getIntent().getStringExtra(EXTRA_GROUP_ID));
        }

        $(GroupMessagesHandler.class).attach(messagesRecyclerView, replyMessage, sendButton);
        $(GroupContactsHandler.class).attach($(GroupHandler.class).getGroup(), contactsRecyclerView, searchContacts);
        peopleInGroup.setSelected(true);
        peopleInGroup.setOnClickListener(view -> toggleContactsView());

        showPhoneContactsButton.setOnClickListener(view -> {
            $(PermissionHandler.class).check(READ_CONTACTS).when(granted -> {
                if (granted) {
                    $(GroupContactsHandler.class).showContactsForQuery();
                    showPhoneContactsButton.setVisibility(View.GONE);
                }
            });
        });
    }

    @Override
    protected int getBackgroundId() {
        return R.id.background;
    }

    private void toggleContactsView() {
        if (replyMessage.getVisibility() == View.GONE) {
            replyMessage.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.VISIBLE);
            searchContacts.setVisibility(View.GONE);
            contactsRecyclerView.setVisibility(View.GONE);
            showPhoneContactsButton.setVisibility(View.GONE);
        } else {
            replyMessage.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
            messagesRecyclerView.setVisibility(View.GONE);
            searchContacts.setVisibility(View.VISIBLE);
            contactsRecyclerView.setVisibility(View.VISIBLE);

            if(!$(PermissionHandler.class).has(READ_CONTACTS) && $(GroupContactsHandler.class).isEmpty()) {
                showPhoneContactsButton.setVisibility(View.VISIBLE);
            }

            if($(PermissionHandler.class).has(READ_CONTACTS)) {
                $(GroupContactsHandler.class).showContactsForQuery();
            }

            searchContacts.setText("");
        }
    }
}
