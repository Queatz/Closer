package closer.vlllage.com.closer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Callback;

import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.PermissionHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.event.EventDetailsHandler;
import closer.vlllage.com.closer.handler.event.EventHandler;
import closer.vlllage.com.closer.handler.group.GroupActionHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.group.GroupContactsHandler;
import closer.vlllage.com.closer.handler.group.GroupHandler;
import closer.vlllage.com.closer.handler.group.GroupMemberHandler;
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler;
import closer.vlllage.com.closer.handler.group.GroupMessageMentionHandler;
import closer.vlllage.com.closer.handler.group.GroupMessagesHandler;
import closer.vlllage.com.closer.handler.group.GroupViewHolder;
import closer.vlllage.com.closer.handler.group.PhysicalGroupUpgradeHandler;
import closer.vlllage.com.closer.handler.group.PinnedMessagesHandler;
import closer.vlllage.com.closer.handler.group.SearchGroupsAdapter;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.GroupColorHandler;
import closer.vlllage.com.closer.handler.helpers.GroupScopeHandler;
import closer.vlllage.com.closer.handler.helpers.ImageHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.handler.helpers.MiniWindowHandler;
import closer.vlllage.com.closer.handler.helpers.OutboundHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.helpers.SystemSettingsHandler;
import closer.vlllage.com.closer.handler.helpers.TimerHandler;
import closer.vlllage.com.closer.handler.helpers.TopHandler;
import closer.vlllage.com.closer.handler.map.MapActivityHandler;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMember;
import closer.vlllage.com.closer.store.models.GroupMember_;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.ui.CircularRevealActivity;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;
import jp.wasabeef.picasso.transformations.BlurTransformation;

import static android.Manifest.permission.READ_CONTACTS;
import static com.google.android.gms.common.util.Strings.isEmptyOrWhitespace;

public class GroupActivity extends CircularRevealActivity {

    public static final String EXTRA_GROUP_ID = "groupId";
    public static final String EXTRA_RESPOND = "respond";

    public GroupViewHolder view;
    public String groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        view = new GroupViewHolder(findViewById(android.R.id.content));

        findViewById(R.id.closeButton).setOnClickListener(view -> finish());

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        $(TimerHandler.class).postDisposable(() -> $(RefreshHandler.class).refreshAll(), 1625);

        $(GroupHandler.class).attach(view.groupName, view.groupAbout, view.peopleInGroup, findViewById(R.id.settingsButton));
        handleIntent(getIntent());

        view.groupAbout.setOnClickListener(v -> {
            if ($(GroupHandler.class).getGroup() == null || $(GroupHandler.class).getGroup().getAbout() == null) {
                return;
            }

            $(DefaultAlerts.class).message(
                    $(ResourcesHandler.class).getResources().getString(R.string.about_this_group),
                    $(GroupHandler.class).getGroup().getAbout()
            );
        });

        $(GroupActionHandler.class).attach(view.actionFrameLayout, findViewById(R.id.actionRecyclerView));
        $(GroupMessagesHandler.class).attach(view.messagesRecyclerView, view.replyMessage, view.sendButton, view.sendMoreButton, findViewById(R.id.sendMoreLayout));
        $(PinnedMessagesHandler.class).attach(view.pinnedMessagesRecyclerView);
        $(GroupMessageMentionHandler.class).attach(view.mentionSuggestionsLayout, findViewById(R.id.mentionSuggestionRecyclerView), mention -> {
            $(GroupMessagesHandler.class).insertMention(mention);
        });
        $(MiniWindowHandler.class).attach(view.groupName, findViewById(R.id.backgroundColor), this::finish);

        findViewById(R.id.settingsButton).setOnClickListener(view -> {
            $(GroupMemberHandler.class).changeGroupSettings($(GroupHandler.class).getGroup());
        });

        if ($(PersistenceHandler.class).getPhoneId() != null) {
            $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupMember.class).query()
                    .equal(GroupMember_.group, groupId)
                    .equal(GroupMember_.phone, $(PersistenceHandler.class).getPhoneId())
                    .build().subscribe().on(AndroidScheduler.mainThread()).observer(groupMembers -> {
                        GroupMember groupMember = groupMembers.isEmpty() ? null : groupMembers.get(0);

                        if (groupMember == null) {
                            groupMember = new GroupMember();
                        }

                        if (groupMember.isMuted()) {
                            findViewById(R.id.notificationSettingsButton).setOnClickListener(view -> $(GroupMemberHandler.class).changeGroupSettings($(GroupHandler.class).getGroup()));
                            findViewById(R.id.notificationSettingsButton).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.notificationSettingsButton).setVisibility(View.GONE);
                        }
                    }));
        }

        view.replyMessage.setOnClickListener(view -> {
            $(GroupActionHandler.class).show(false);
            cancelShare();
        });

        $(DisposableHandler.class).add($(GroupHandler.class).onGroupUpdated().subscribe(group -> {
            $(PinnedMessagesHandler.class).show(group);

            setGroupBackground(group);
            refreshPhysicalGroupActions(group);
        }));

        $(DisposableHandler.class).add($(GroupHandler.class).onGroupChanged().subscribe(group -> {
            $(PinnedMessagesHandler.class).show(group);

            view.actionCancel.setVisibility(View.GONE);
            view.actionShare.setVisibility(View.VISIBLE);
            view.actionShowOnMap.setVisibility(View.VISIBLE);
            view.actionSettingsSetName.setVisibility(View.GONE);
            view.actionSettingsSetBackground.setVisibility(View.GONE);
            view.actionSettingsGetDirections.setVisibility(View.GONE);
            view.actionSettingsHostEvent.setVisibility(View.GONE);

            findViewById(R.id.backgroundColor).setBackgroundResource($(GroupColorHandler.class).getColorBackground(group));

            refreshPhysicalGroupActions(group);
            cancelShare();

            view.actionSettingsGetDirections.setOnClickListener(view -> {
                $(OutboundHandler.class).openDirections(new LatLng(
                        group.getLatitude(),
                        group.getLongitude()
                ));
            });

            $(GroupScopeHandler.class).setup(group, view.scopeIndicatorButton);

            view.peopleInGroup.setSelected(true);
            view.peopleInGroup.setOnClickListener(view -> toggleContactsView());

            $(GroupContactsHandler.class).attach(group, view.contactsRecyclerView, view.searchContacts, view.showPhoneContactsButton);

            view.showPhoneContactsButton.setOnClickListener(v -> {
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
                        view.showPhoneContactsButton.setVisibility(View.GONE);
                    }
                });
            });

            if (group.isPhysical()) {
                view.eventToolbar.setVisibility(View.VISIBLE);
                view.actionShare.setVisibility(View.GONE);
                view.actionCancel.setVisibility(View.GONE);
                view.actionShowOnMap.setVisibility(View.VISIBLE);
                view.actionShowOnMap.setOnClickListener(view -> showGroupOnMap(group));

                view.actionSettingsSetName.setOnClickListener(view -> $(PhysicalGroupUpgradeHandler.class).convertToHub(group, updatedGroup -> {
                    $(GroupHandler.class).showGroupName(updatedGroup);
                    refreshPhysicalGroupActions(updatedGroup);
                }));
                view.actionSettingsSetBackground.setOnClickListener(view -> $(PhysicalGroupUpgradeHandler.class).setBackground(group, updateGroup -> {
                    setGroupBackground(updateGroup);
                    refreshPhysicalGroupActions(updateGroup);
                }));
                view.actionSettingsHostEvent.setOnClickListener(view -> {
                    $(EventHandler.class).createNewEvent(new LatLng(
                            group.getLatitude(),
                            group.getLongitude()
                    ), group.isPublic(), this::showEventOnMap);
                });
            }
            setGroupBackground(group);
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));

        $(DisposableHandler.class).add($(GroupHandler.class).onEventChanged().subscribe(event -> {
            view.groupDetails.setVisibility(View.VISIBLE);
            view.eventToolbar.setVisibility(View.VISIBLE);
            view.groupDetails.setText($(EventDetailsHandler.class).formatEventDetails(event));

            view.actionShare.setOnClickListener(view -> share(event));
            view.actionShowOnMap.setOnClickListener(view -> showEventOnMap(event));

            if ($(PersistenceHandler.class).getPhoneId() != null) {
                if (!event.isCancelled() && event.getCreator() != null && new Date().before(event.getEndsAt()) && event.getCreator().equals($(PersistenceHandler.class).getPhoneId())) {
                    view.actionCancel.setOnClickListener(view -> {
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
                }
            } else {
                view.actionCancel.setVisibility(View.GONE);
            }
        }, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
        setSourceBounds(intent.getSourceBounds());
        reveal();
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_GROUP_ID)) {
            groupId = intent.getStringExtra(EXTRA_GROUP_ID);
            $(GroupHandler.class).setGroupById(groupId);

            if (intent.hasExtra(EXTRA_RESPOND)) {
                view.replyMessage.postDelayed(() -> {
                    view.replyMessage.requestFocus();
                    $(KeyboardHandler.class).showKeyboard(view.replyMessage, true);
                }, 500);
            }
        }
    }

    private void refreshPhysicalGroupActions(Group group) {
        if (group.isPhysical()) {
            int buttonCount = 1;

            if (isEmptyOrWhitespace(group.getName())) {
                view.actionSettingsSetName.setVisibility(View.VISIBLE);
                buttonCount++;
            } else {
                view.actionSettingsSetName.setVisibility(View.GONE);
            }

            if (isEmptyOrWhitespace(group.getPhoto())) {
                view.actionSettingsSetBackground.setVisibility(View.VISIBLE);
                buttonCount++;
            } else {
                view.actionSettingsSetBackground.setVisibility(View.GONE);
            }

            if (buttonCount < 3) {
                view.actionSettingsGetDirections.setVisibility(View.VISIBLE);
                buttonCount++;
            } else {
                view.actionSettingsGetDirections.setVisibility(View.GONE);
            }

            if (buttonCount < 3) {
                view.actionSettingsHostEvent.setVisibility(View.VISIBLE);
            } else {
                view.actionSettingsHostEvent.setVisibility(View.GONE);
            }
        }
    }

    public void setGroupBackground(Group group) {
        ImageView backgroundPhoto = findViewById(R.id.backgroundPhoto);
        if (group.getPhoto() != null) {
            backgroundPhoto.setVisibility(View.VISIBLE);
            backgroundPhoto.setImageDrawable(null);
            $(ImageHandler.class).get().load(group.getPhoto() + "?s=32").transform(new BlurTransformation(this, 2)).into(backgroundPhoto, new Callback() {
                @Override
                public void onSuccess() {
                    $(ImageHandler.class).get().load(group.getPhoto() + "?s=512").noPlaceholder().into(backgroundPhoto);
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

        showMessagesView(true);

        view.shareWithRecyclerView.setVisibility(View.VISIBLE);
        view.messagesLayoutGroup.setVisibility(View.GONE);
        view.sendMoreButton.setVisibility(View.GONE);
        view.actionShare.setText(R.string.cancel);

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

        view.shareWithRecyclerView.setAdapter(searchGroupsAdapter);
        view.shareWithRecyclerView.setLayoutManager(new LinearLayoutManager(
                view.shareWithRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        ));
    }

    private boolean cancelShare() {
        if (view.shareWithRecyclerView.getVisibility() != View.VISIBLE) {
            return false;
        }

        view.shareWithRecyclerView.setVisibility(View.GONE);
        view.messagesLayoutGroup.setVisibility(View.VISIBLE);
        view.actionShare.setText(R.string.share);
        return true;
    }

    private void toggleContactsView() {
        showMessagesView(view.replyMessage.getVisibility() == View.GONE);
    }

    private void showMessagesView(boolean show) {
        cancelShare();

        $(GroupMessagesHandler.class).showSendMoreOptions(false);

        if (show) {
            view.messagesLayoutGroup.setVisibility(View.VISIBLE);
            view.membersLayoutGroup.setVisibility(View.GONE);

            if (view.replyMessage.getText().toString().isEmpty()) {
                view.sendMoreButton.setVisibility(View.VISIBLE);
            } else {
                view.sendMoreButton.setVisibility(View.GONE);
            }

            view.showPhoneContactsButton.setVisibility(View.GONE);
        } else {
            view.messagesLayoutGroup.setVisibility(View.GONE);
            view.membersLayoutGroup.setVisibility(View.VISIBLE);

            view.sendMoreButton.setVisibility(View.GONE);

            $(GroupActionHandler.class).cancelPendingAnimation();

            if(!$(PermissionHandler.class).has(READ_CONTACTS)) {
                view.showPhoneContactsButton.setVisibility(View.VISIBLE);
            }

            if($(PermissionHandler.class).has(READ_CONTACTS)) {
                $(GroupContactsHandler.class).showContactsForQuery();
            }

            view.searchContacts.setText("");
        }
    }
}
