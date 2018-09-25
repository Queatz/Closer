package closer.vlllage.com.closer;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.api.models.GroupMemberResult;
import closer.vlllage.com.closer.handler.FeatureHandler;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.PermissionHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.event.EventDetailsHandler;
import closer.vlllage.com.closer.handler.group.GroupActionHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.group.GroupContactsHandler;
import closer.vlllage.com.closer.handler.group.GroupHandler;
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler;
import closer.vlllage.com.closer.handler.group.GroupMessagesHandler;
import closer.vlllage.com.closer.handler.group.PhysicalGroupUpgradeHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.handler.helpers.MenuHandler;
import closer.vlllage.com.closer.handler.helpers.MiniWindowHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.helpers.SystemSettingsHandler;
import closer.vlllage.com.closer.handler.helpers.TimerHandler;
import closer.vlllage.com.closer.handler.helpers.TopHandler;
import closer.vlllage.com.closer.handler.map.MapActivityHandler;
import closer.vlllage.com.closer.handler.search.SearchGroupsAdapter;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMember;
import closer.vlllage.com.closer.store.models.GroupMember_;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.ui.CircularRevealActivity;
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;
import jp.wasabeef.picasso.transformations.BlurTransformation;

import static android.Manifest.permission.READ_CONTACTS;
import static closer.vlllage.com.closer.handler.FeatureType.FEATURE_UPGRADE_PHYSICAL_GROUPS;
import static com.google.android.gms.common.util.Strings.isEmptyOrWhitespace;

public class GroupActivity extends CircularRevealActivity {

    public static final String EXTRA_GROUP_ID = "groupId";
    public static final String EXTRA_RESPOND = "respond";

    private TextView peopleInGroup;
    private TextView groupName;
    private TextView groupDetails;
    private View eventToolbar;
    private Button actionShare;
    private Button actionShowOnMap;
    private Button actionCancel;
    private Button actionSettingsSetName;
    private Button actionSettingsSetBackground;
    private MaxSizeFrameLayout actionFrameLayout;
    private EditText replyMessage;
    private RecyclerView messagesRecyclerView;
    private RecyclerView shareWithRecyclerView;
    private EditText searchContacts;
    private RecyclerView contactsRecyclerView;
    private Button showPhoneContactsButton;
    private ImageButton sendButton;
    private ImageButton sendMoreButton;
    private String groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        replyMessage = findViewById(R.id.replyMessage);
        sendMoreButton = findViewById(R.id.sendMoreButton);
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
        actionSettingsSetName = findViewById(R.id.actionSettingsSetName);
        actionSettingsSetBackground = findViewById(R.id.actionSettingsSetBackground);
        actionFrameLayout = findViewById(R.id.actionFrameLayout);

        findViewById(R.id.closeButton).setOnClickListener(view -> finish());

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        $(TimerHandler.class).postDisposable(() -> $(RefreshHandler.class).refreshAll(), 1625);

        $(GroupHandler.class).attach(groupName, peopleInGroup, findViewById(R.id.settingsButton));
        if (getIntent() != null && getIntent().hasExtra(EXTRA_GROUP_ID)) {
            groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
            $(GroupHandler.class).setGroupById(groupId);

            if (getIntent().hasExtra(EXTRA_RESPOND)) {
                replyMessage.postDelayed(() -> {
                    replyMessage.requestFocus();
                    $(KeyboardHandler.class).showKeyboard(replyMessage, true);
                }, 500);
            }
        }

        $(GroupMessagesHandler.class).attach(messagesRecyclerView, replyMessage, sendButton, sendMoreButton, findViewById(R.id.sendMoreLayout));
        $(GroupActionHandler.class).attach(actionFrameLayout, findViewById(R.id.actionRecyclerView));
        $(MiniWindowHandler.class).attach(groupName, findViewById(R.id.backgroundColor), this::finish);

        findViewById(R.id.settingsButton).setOnClickListener(view -> {
            changeGroupSettings();
        });

        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupMember.class).query()
                .equal(GroupMember_.group, groupId)
                .equal(GroupMember_.phone, $(PersistenceHandler.class).getPhoneId())
                .build().subscribe().on(AndroidScheduler.mainThread()).observer(groupMembers -> {
                    GroupMember groupMember = groupMembers.isEmpty() ? null : groupMembers.get(0);

                    if (groupMember == null) {
                        groupMember = new GroupMember();
                    }

                    if (groupMember.isMuted()) {
                        findViewById(R.id.notificationSettingsButton).setOnClickListener(view -> changeGroupSettings());
                        findViewById(R.id.notificationSettingsButton).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.notificationSettingsButton).setVisibility(View.GONE);
                    }
                }));


        replyMessage.setOnClickListener(view -> {
            $(GroupActionHandler.class).show(false);
            cancelShare();
        });

        $(DisposableHandler.class).add($(GroupHandler.class).onGroupChanged().subscribe(group -> {
            if (group.isPublic()) {
                if (group.hasEvent()) {
                    findViewById(R.id.backgroundColor).setBackgroundResource(R.drawable.color_red_rounded);
                } else if (group.isPhysical()) {
                    findViewById(R.id.backgroundColor).setBackgroundResource(R.drawable.color_purple_rounded);
                    eventToolbar.setVisibility(View.VISIBLE);
                    actionShare.setVisibility(View.GONE);
                    actionCancel.setVisibility(View.GONE);
                    actionShowOnMap.setVisibility(View.VISIBLE);
                    actionShowOnMap.setOnClickListener(view -> showGroupOnMap(group));

                    if ($(FeatureHandler.class).has(FEATURE_UPGRADE_PHYSICAL_GROUPS)) {
                        refreshPhysicalGroupActions(group);
                        actionSettingsSetName.setOnClickListener(view -> $(PhysicalGroupUpgradeHandler.class).convertToHub(group, updatedGroup -> {
                            $(GroupHandler.class).showGroupName(updatedGroup);
                            refreshPhysicalGroupActions(updatedGroup);
                        }));
                        actionSettingsSetBackground.setOnClickListener(view -> $(PhysicalGroupUpgradeHandler.class).setBackground(group, updateGroup -> {
                            setGroupBackground(updateGroup);
                            refreshPhysicalGroupActions(updateGroup);
                        }));
                    }
                } else {
                    findViewById(R.id.backgroundColor).setBackgroundResource(R.drawable.color_green_rounded);
                }

                setGroupBackground(group);
            } else {
                $(GroupContactsHandler.class).attach(group, contactsRecyclerView, searchContacts, showPhoneContactsButton);
                peopleInGroup.setSelected(true);
                peopleInGroup.setOnClickListener(view -> toggleContactsView());

                showPhoneContactsButton.setOnClickListener(view -> {
                    if ($(PermissionHandler.class).denied(READ_CONTACTS)) {
                        $(AlertHandler.class).make()
                                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.enable_contacts_permission))
                                .setMessage($(ResourcesHandler.class).getResources().getString(R.string.enable_contacts_permission_rationale))
                                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.open_settings))
                                .setPositiveButtonCallback(alertResult -> $(SystemSettingsHandler.class).showSystemSettings())
                                .show();
                        return;
                    }

                    $(PermissionHandler.class).check(READ_CONTACTS).when(granted -> {
                        if (granted) {
                            $(GroupContactsHandler.class).showContactsForQuery();
                            showPhoneContactsButton.setVisibility(View.GONE);
                        }
                    });
                });
            }
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));

        $(DisposableHandler.class).add($(GroupHandler.class).onEventChanged().subscribe(event -> {
            groupDetails.setVisibility(View.VISIBLE);
            eventToolbar.setVisibility(View.VISIBLE);
            groupDetails.setText($(EventDetailsHandler.class).formatEventDetails(event));

            actionShare.setOnClickListener(view -> share(event));
            actionShowOnMap.setOnClickListener(view -> showEventOnMap(event));

            if (!event.isCancelled() && event.getCreator() != null && new Date().before(event.getEndsAt()) && event.getCreator().equals($(PersistenceHandler.class).getPhoneId())) {
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
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    private void changeGroupSettings() {
        if ($(GroupHandler.class).getGroup() == null) {
            $(DefaultAlerts.class).thatDidntWork();
            return;
        }

        if ($(GroupHandler.class).getGroup().isPublic()) {
            $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupMember.class).query()
                    .equal(GroupMember_.group, groupId)
                    .equal(GroupMember_.phone, $(PersistenceHandler.class).getPhoneId())
                    .build().subscribe().single().on(AndroidScheduler.mainThread()).observer(groupMembers -> {
                        if (groupMembers.isEmpty()) {
                            $(DisposableHandler.class).add($(ApiHandler.class).getGroupMember(groupId)
                                    .map(GroupMemberResult::from)
                                    .doOnNext($(StoreHandler.class).getStore().box(GroupMember.class)::put)
                                    .subscribe(this::setupGroupMember, error -> this.setupGroupMember(null)));
                        } else {
                            setupGroupMember(groupMembers.get(0));
                        }
                    }));
        } else {
            $(MenuHandler.class).show(
                    new MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_an_action, () -> {
                        if ($(GroupHandler.class).getGroup() != null) {
                            $(GroupActionHandler.class).addActionToGroup($(GroupHandler.class).getGroup());
                        }
                    }));
        }
    }

    private void setupGroupMember(GroupMember groupMember) {
        if (groupMember == null) {
            groupMember = new GroupMember();
            groupMember.setGroup(groupId);
            groupMember.setPhone($(PersistenceHandler.class).getPhoneId());
        }

        @StringRes int subscribeText = groupMember.isSubscribed() ? R.string.unsubscribe : R.string.subscribe;
        @DrawableRes int subscribeIcon = groupMember.isSubscribed() ? R.drawable.ic_baseline_check_circle_24px : R.drawable.ic_baseline_check_circle_outline_24px;
        @StringRes int muteText = groupMember.isMuted() ? R.string.unmute_notifications : R.string.mute_notifications;
        @DrawableRes int muteIcon = groupMember.isMuted() ? R.drawable.ic_notifications_off_black_24dp : R.drawable.ic_notifications_none_black_24dp;

        final GroupMember updatedGroupMember = groupMember;
        $(MenuHandler.class).show(
                new MenuHandler.MenuOption(subscribeIcon, subscribeText, () -> {
                    updatedGroupMember.setSubscribed(!updatedGroupMember.isSubscribed());
                    $(SyncHandler.class).sync(updatedGroupMember);
                }),
                new MenuHandler.MenuOption(muteIcon, muteText, () -> {
                    updatedGroupMember.setMuted(!updatedGroupMember.isMuted());
                    $(SyncHandler.class).sync(updatedGroupMember);
                }),
                new MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_an_action, () -> {
                    if ($(GroupHandler.class).getGroup() != null) {
                        $(GroupActionHandler.class).addActionToGroup($(GroupHandler.class).getGroup());
                    }
                })
        );
    }

    private void refreshPhysicalGroupActions(Group group) {
        if (isEmptyOrWhitespace(group.getName())) {
            actionSettingsSetName.setVisibility(View.VISIBLE);
        } else {
            actionSettingsSetName.setVisibility(View.GONE);
        }

        if (isEmptyOrWhitespace(group.getPhoto())) {
            actionSettingsSetBackground.setVisibility(View.VISIBLE);
        } else {
            actionSettingsSetBackground.setVisibility(View.GONE);
        }
    }

    public void setGroupBackground(Group group) {
        ImageView backgroundPhoto = findViewById(R.id.backgroundPhoto);
        if (group.getPhoto() != null) {
            backgroundPhoto.setVisibility(View.VISIBLE);
            backgroundPhoto.setImageDrawable(null);
            Picasso.get().load(group.getPhoto() + "?s=32").transform(new BlurTransformation(this, 2)).into(backgroundPhoto, new Callback() {
                @Override
                public void onSuccess() {
                    Picasso.get().load(group.getPhoto() + "?s=512").noPlaceholder().into(backgroundPhoto);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    onSuccess();
                }
            });
        } else {
            backgroundPhoto.setVisibility(View.GONE);
        }
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

    private void showGroupOnMap(Group group) {
        ((CircularRevealActivity) $(ActivityHandler.class).getActivity())
                .finish(() -> $(MapActivityHandler.class).showGroupOnMap(group));
    }

    private void share(Event event) {
        if (cancelShare()) {
            return;
        }

        shareWithRecyclerView.setVisibility(View.VISIBLE);
        messagesRecyclerView.setVisibility(View.GONE);
        actionShare.setText(R.string.cancel);

        QueryBuilder<Group> queryBuilder = $(StoreHandler.class).getStore().box(Group.class).query();
        List<Group> groups = queryBuilder.sort($(SortHandler.class).sortGroups()).notEqual(Group_.physical, true).build().find();

        SearchGroupsAdapter searchGroupsAdapter = new SearchGroupsAdapter($(GroupHandler.class), (group, view) -> {
            boolean success = $(GroupMessageAttachmentHandler.class).shareEvent(event, group);

            if (success) {
                ((CircularRevealActivity) $(ActivityHandler.class).getActivity())
                        .finish(() -> $(GroupActivityTransitionHandler.class).showGroupMessages(view, group.getId()));
            } else {
                $(DefaultAlerts.class).thatDidntWork();
            }
        }, null);

        searchGroupsAdapter.setGroups(groups);
        searchGroupsAdapter.setActionText($(ResourcesHandler.class).getResources().getString(R.string.share));
        searchGroupsAdapter.setIsSmall(true);

        shareWithRecyclerView.setAdapter(searchGroupsAdapter);
        shareWithRecyclerView.setLayoutManager(new LinearLayoutManager(
                shareWithRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        ));
    }

    private boolean cancelShare() {
        if (shareWithRecyclerView.getVisibility() != View.VISIBLE) {
            return false;
        }

        shareWithRecyclerView.setVisibility(View.GONE);
        messagesRecyclerView.setVisibility(View.VISIBLE);
        actionShare.setText(R.string.share);
        return true;
    }

    private void toggleContactsView() {
        if (replyMessage.getVisibility() == View.GONE) {
            replyMessage.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.VISIBLE);
            sendMoreButton.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.VISIBLE);
            searchContacts.setVisibility(View.GONE);
            contactsRecyclerView.setVisibility(View.GONE);
            showPhoneContactsButton.setVisibility(View.GONE);
            actionFrameLayout.setVisibility(View.VISIBLE);
        } else {
            replyMessage.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
            sendMoreButton.setVisibility(View.GONE);
            messagesRecyclerView.setVisibility(View.GONE);
            searchContacts.setVisibility(View.VISIBLE);
            contactsRecyclerView.setVisibility(View.VISIBLE);
            actionFrameLayout.setVisibility(View.GONE);
            $(GroupActionHandler.class).cancelPendingAnimation();

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
