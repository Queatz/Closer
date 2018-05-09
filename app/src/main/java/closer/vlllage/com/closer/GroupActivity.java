package closer.vlllage.com.closer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import closer.vlllage.com.closer.handler.AccountHandler;
import closer.vlllage.com.closer.handler.ActivityHandler;
import closer.vlllage.com.closer.handler.AlertHandler;
import closer.vlllage.com.closer.handler.ApiHandler;
import closer.vlllage.com.closer.handler.ApplicationHandler;
import closer.vlllage.com.closer.handler.DefaultAlerts;
import closer.vlllage.com.closer.handler.DisposableHandler;
import closer.vlllage.com.closer.handler.EventDetailsHandler;
import closer.vlllage.com.closer.handler.GroupMessageAttachmentHandler;
import closer.vlllage.com.closer.handler.MapActivityHandler;
import closer.vlllage.com.closer.handler.MiniWindowHandler;
import closer.vlllage.com.closer.handler.PermissionHandler;
import closer.vlllage.com.closer.handler.PersistenceHandler;
import closer.vlllage.com.closer.handler.RefreshHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.handler.SortHandler;
import closer.vlllage.com.closer.handler.TimerHandler;
import closer.vlllage.com.closer.handler.TopHandler;
import closer.vlllage.com.closer.handler.group.GroupContactsHandler;
import closer.vlllage.com.closer.handler.group.GroupHandler;
import closer.vlllage.com.closer.handler.group.GroupMessagesHandler;
import closer.vlllage.com.closer.handler.search.SearchGroupsAdapter;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.query.QueryBuilder;

import static android.Manifest.permission.READ_CONTACTS;

public class GroupActivity extends CircularRevealActivity {

    public static final String EXTRA_GROUP_ID = "groupId";
    private TextView peopleInGroup;
    private TextView groupName;
    private TextView groupDetails;
    private View eventToolbar;
    private Button actionShare;
    private Button actionShowOnMap;
    private Button actionCancel;
    private EditText replyMessage;
    private RecyclerView messagesRecyclerView;
    private RecyclerView shareWithRecyclerView;
    private EditText searchContacts;
    private RecyclerView contactsRecyclerView;
    private Button showPhoneContactsButton;
    private ImageButton sendButton;
    private String groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        replyMessage = findViewById(R.id.replyMessage);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        searchContacts = findViewById(R.id.searchContacts);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        shareWithRecyclerView = findViewById(R.id.shareWithRecyclerView);
        peopleInGroup = findViewById(R.id.peopleInGroup);
        groupDetails = findViewById(R.id.groupDetails);
        groupName = findViewById(R.id.groupName);
        eventToolbar = findViewById(R.id.eventToolbar);
        showPhoneContactsButton = findViewById(R.id.showPhoneContactsButton);
        sendButton = findViewById(R.id.sendButton);
        actionShare = findViewById(R.id.actionShare);
        actionShowOnMap = findViewById(R.id.actionShowOnMap);
        actionCancel = findViewById(R.id.actionCancel);

        findViewById(R.id.closeButton).setOnClickListener(view -> finish());

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        $(TimerHandler.class).postDisposable(() -> $(RefreshHandler.class).refreshAll(), 1625);

        $(GroupHandler.class).attach(groupName, peopleInGroup);
        if (getIntent() != null && getIntent().hasExtra(EXTRA_GROUP_ID)) {
            groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
            $(GroupHandler.class).setGroupById(groupId);
        }

        $(GroupMessagesHandler.class).attach(messagesRecyclerView, replyMessage, sendButton);
        $(MiniWindowHandler.class).attach(findViewById(R.id.groupName), findViewById(R.id.backgroundColor));

        $(DisposableHandler.class).add($(GroupHandler.class).onGroupChanged().subscribe(group -> {
            if (group.isPublic()) {
                if (group.hasEvent()) {
                    findViewById(R.id.backgroundColor).setBackgroundResource(R.color.red);
                } else {
                    findViewById(R.id.backgroundColor).setBackgroundResource(R.color.green);
                }
                peopleInGroup.setVisibility(View.GONE);
            } else {
                $(GroupContactsHandler.class).attach(group, contactsRecyclerView, searchContacts);
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
        }));

        $(DisposableHandler.class).add($(GroupHandler.class).onEventChanged().subscribe(event -> {
            groupDetails.setVisibility(View.VISIBLE);
            eventToolbar.setVisibility(View.VISIBLE);
            groupDetails.setText($(EventDetailsHandler.class).formatEventDetails(event));

            actionShare.setOnClickListener(view -> {
                share(event);
            });
            actionShowOnMap.setOnClickListener(view -> showEventOnMap(event));

            if (!event.isCancelled() && event.getCreator() != null && event.getCreator().equals($(PersistenceHandler.class).getPhoneId())) {
                actionCancel.setOnClickListener(view -> {
                    $(AlertHandler.class).make()
                            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.cancel_event))
                            .setMessage($(ResourcesHandler.class).getResources().getString(R.string.event_will_be_cancelled, event.getName()))
                            .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.cancel_event))
                            .setPositiveButtonCallback(result -> {
                                $(DisposableHandler.class).add($(ApiHandler.class).cancelEvent(event.getId()).subscribe(successResult -> {
                                    if (successResult.success) {
                                        $(DefaultAlerts.class).message($(ResourcesHandler.class).getResources().getString(R.string.event_cancelled, event.getName()));
                                        $(RefreshHandler.class).refreshEvents(new LatLng(event.getLatitude(), event.getLongitude()));
                                    } else {
                                        $(DefaultAlerts.class).thatDidntWork();
                                    }
                                }, error -> $(DefaultAlerts.class).thatDidntWork()));
                            })
                            .show();
                });
            } else {
                actionCancel.setVisibility(View.GONE);
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        $(ApplicationHandler.class).getApp().$(TopHandler.class).setGroupActive(groupId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        $(ApplicationHandler.class).getApp().$(TopHandler.class).setGroupActive(null);
    }

    @Override
    protected int getBackgroundId() {
        return R.id.background;
    }

    private void showEventOnMap(Event event) {
        ((CircularRevealActivity) $(ActivityHandler.class).getActivity())
                .finish(() -> $(MapActivityHandler.class).showEventOnMap(event));

    }

    private void share(Event event) {
        if (shareWithRecyclerView.getVisibility() == View.VISIBLE) {
            shareWithRecyclerView.setVisibility(View.GONE);
            messagesRecyclerView.setVisibility(View.VISIBLE);
            actionShare.setText(R.string.share);
            return;
        }

        shareWithRecyclerView.setVisibility(View.VISIBLE);
        messagesRecyclerView.setVisibility(View.GONE);
        actionShare.setText(R.string.cancel);

        QueryBuilder<Group> queryBuilder = $(StoreHandler.class).getStore().box(Group.class).query()
                .equal(Group_.isPublic, true);

        List<Group> groups = queryBuilder.sort($(SortHandler.class).sortGroups()).build().find();

        SearchGroupsAdapter searchGroupsAdapter = new SearchGroupsAdapter($(GroupHandler.class), group -> {
            $(GroupMessageAttachmentHandler.class).shareEvent(event, group);
        }, null);

        searchGroupsAdapter.setGroups(groups);
        searchGroupsAdapter.setActionText($(ResourcesHandler.class).getResources().getString(R.string.share));

        shareWithRecyclerView.setAdapter(searchGroupsAdapter);
        shareWithRecyclerView.setLayoutManager(new LinearLayoutManager(
                shareWithRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        ));
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
