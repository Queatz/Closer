package closer.vlllage.com.closer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import closer.vlllage.com.closer.handler.group.GroupContactsHandler;
import closer.vlllage.com.closer.handler.group.GroupHandler;
import closer.vlllage.com.closer.handler.group.GroupMessagesHandler;

public class GroupActivity extends CircularRevealActivity {

    public static final String EXTRA_GROUP_ID = "groupId";
    private TextView peopleInGroup;
    private TextView groupName;
    private EditText replyMessage;
    private RecyclerView messagesRecyclerView;
    private EditText searchContacts;
    private RecyclerView contactsRecyclerView;

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

        findViewById(R.id.closeButton).setOnClickListener(view -> finish());

        $(GroupHandler.class).attach(groupName, peopleInGroup);
        $(GroupMessagesHandler.class).attach(messagesRecyclerView, replyMessage);
        $(GroupContactsHandler.class).attach(contactsRecyclerView, searchContacts);
        peopleInGroup.setSelected(true);
        peopleInGroup.setOnClickListener(view -> toggleContactsView());

        if (getIntent() != null && getIntent().hasExtra(EXTRA_GROUP_ID)) {
            $(GroupHandler.class).setGroupById(getIntent().getStringExtra(EXTRA_GROUP_ID));
        }
    }

    @Override
    protected int getBackgroundId() {
        return R.id.background;
    }

    private void toggleContactsView() {
        if (replyMessage.getVisibility() == View.GONE) {
            replyMessage.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.VISIBLE);
            searchContacts.setVisibility(View.GONE);
            contactsRecyclerView.setVisibility(View.GONE);
        } else {
            replyMessage.setVisibility(View.GONE);
            messagesRecyclerView.setVisibility(View.GONE);
            searchContacts.setVisibility(View.VISIBLE);
            contactsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
