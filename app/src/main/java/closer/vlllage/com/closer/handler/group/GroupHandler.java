package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.essentials.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.api.models.EventResult;
import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.handler.FeatureHandler;
import closer.vlllage.com.closer.handler.FeatureType;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.phone.NameHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Event_;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupInvite;
import closer.vlllage.com.closer.store.models.GroupInvite_;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;
import io.reactivex.subjects.BehaviorSubject;

import static android.text.format.DateUtils.DAY_IN_MILLIS;

public class GroupHandler extends PoolMember {

    private TextView groupName;
    private TextView peopleInGroup;
    private View settingsButton;
    private Group group;
    private GroupContact groupContact;
    private BehaviorSubject<Group> groupChanged = BehaviorSubject.create();
    private BehaviorSubject<Event> eventChanged = BehaviorSubject.create();
    private List<String> contactNames = new ArrayList<>();
    private List<String> contactInvites = new ArrayList<>();

    public void attach(TextView groupName, TextView peopleInGroup, View settingsButton) {
        this.groupName = groupName;
        this.peopleInGroup = peopleInGroup;
        this.settingsButton = settingsButton;
    }

    public void setGroupById(String groupId) {
        if (groupId == null) {
            setGroup(null);
            return;
        }

        setGroup($(StoreHandler.class).getStore().box(Group.class).query()
                .equal(Group_.id, groupId)
                .build().findFirst());

        if (group == null) {
            $(DisposableHandler.class).add($(ApiHandler.class).getGroup(groupId)
                    .map(GroupResult::from)
                    .subscribe(group -> {
                        $(RefreshHandler.class).refresh(group);
                        setGroup(group);
                    }));
        }
    }

    private void onGroupSet(@NonNull Group group) {
        setGroupContact();
        peopleInGroup.setText("");
        String phoneId = $(PersistenceHandler.class).getPhoneId();
        if (phoneId == null) phoneId = "";
        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupContact.class).query()
                .equal(GroupContact_.groupId, group.getId())
                .notEqual(GroupContact_.contactId, phoneId)
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer(groupContacts -> {
                    contactNames = new ArrayList<>();
                    for (GroupContact groupContact : groupContacts) {
                        if (isInactive(groupContact)) {
                            contactNames.add($(ResourcesHandler.class).getResources().getString(R.string.contact_inactive_inline, $(NameHandler.class).getName(groupContact)));
                        } else {
                            contactNames.add($(NameHandler.class).getName(groupContact));
                        }
                    }

                    redrawContacts();
                }));

        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupInvite.class).query()
                .equal(GroupInvite_.group, group.getId())
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer(groupInvites -> {
                    contactInvites = new ArrayList<>();
                    for (GroupInvite groupInvite : groupInvites) {
                        contactInvites.add($(ResourcesHandler.class).getResources().getString(R.string.contact_invited_inline, $(NameHandler.class).getName(groupInvite)));
                    }
                    redrawContacts();
                }));

        if ($(FeatureHandler.class).has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
            settingsButton.setVisibility(View.VISIBLE);
        }
    }

    private void setGroupContact() {
        if ($(PersistenceHandler.class).getPhoneId() == null) {
            return;
        }

        groupContact = $(StoreHandler.class).getStore().box(GroupContact.class).query()
                .equal(GroupContact_.groupId, group.getId())
                .equal(GroupContact_.contactId, $(PersistenceHandler.class).getPhoneId())
                .build().findFirst();
    }

    private boolean isInactive(GroupContact groupContact) {
        Date fifteenDaysAgo = new Date();
        fifteenDaysAgo.setTime(fifteenDaysAgo.getTime() - 15 * DAY_IN_MILLIS);
        return groupContact.getContactActive().before(fifteenDaysAgo);
    }

    private void redrawContacts() {
        if (group != null && group.isPublic()) {
            if ($(Val.class).isEmpty(group.getAbout())) {
                peopleInGroup.setVisibility(View.GONE);
            } else {
                peopleInGroup.setText(group.getAbout());
                peopleInGroup.setVisibility(View.VISIBLE);
                peopleInGroup.setBackground(null);
            }
        } else {
            peopleInGroup.setBackgroundResource(R.drawable.clickable_light);
            peopleInGroup.setVisibility(View.VISIBLE);

            List<String> names = new ArrayList<>();

            names.addAll(contactNames);
            names.addAll(contactInvites);

            if (names.isEmpty()) {
                peopleInGroup.setText(R.string.add_contact);
                return;
            }

            peopleInGroup.setText(StringUtils.join(names, ", "));
        }
    }

    private void setGroup(Group group) {
        this.group = group;

        if (group != null) {
            onGroupSet(group);
            groupChanged.onNext(group);
            setEventById(group.getEventId());
        }

        showGroupName(group);
    }

    public void showGroupName(Group group) {
        if (group == null) {
            groupName.setText(R.string.not_found);
            return;
        }

        groupName.setText($(Val.class).of(group.getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)));
    }

    private void setEventById(String eventId) {
        if (eventId == null) {
            return;
        }

        Event event = $(StoreHandler.class).getStore().box(Event.class).query()
                .equal(Event_.id, eventId)
                .build().findFirst();

        if (event != null) {
            eventChanged.onNext(event);
        } else {
            $(DisposableHandler.class).add($(ApiHandler.class).getEvent(eventId).map(EventResult::from).subscribe(eventFromServer -> {
                $(RefreshHandler.class).refresh(eventFromServer);
                eventChanged.onNext(eventFromServer);
            }, error -> $(DefaultAlerts.class).thatDidntWork()));
        }
    }

    public Group getGroup() {
        return group;
    }

    public GroupContact getGroupContact() {
        return groupContact;
    }

    public BehaviorSubject<Group> onGroupChanged() {
        return groupChanged;
    }

    public BehaviorSubject<Event> onEventChanged() {
        return eventChanged;
    }
}
