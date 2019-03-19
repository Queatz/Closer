package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.essentials.StringUtils;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.handler.FeatureHandler;
import closer.vlllage.com.closer.handler.FeatureType;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.DataHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.PhotoLoader;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.phone.NameHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupInvite;
import closer.vlllage.com.closer.store.models.GroupInvite_;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.store.models.Phone;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.DataSubscription;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class GroupHandler extends PoolMember {

    private TextView groupName;
    private TextView groupAbout;
    private ImageView backgroundPhoto;
    private TextView peopleInGroup;
    private View settingsButton;
    private Group group;
    private GroupContact groupContact;
    private BehaviorSubject<Group> groupChanged = BehaviorSubject.create();
    private PublishSubject<Group> groupUpdated = PublishSubject.create();
    private BehaviorSubject<Event> eventChanged = BehaviorSubject.create();
    private BehaviorSubject<Phone> phoneChanged = BehaviorSubject.create();
    private List<String> contactNames = new ArrayList<>();
    private List<String> contactInvites = new ArrayList<>();
    private DataSubscription groupDataSubscription;

    public void attach(TextView groupName, ImageView backgroundPhoto, TextView groupAbout, TextView peopleInGroup, View settingsButton) {
        this.groupName = groupName;
        this.backgroundPhoto = backgroundPhoto;
        this.groupAbout = groupAbout;
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

        if ($(Val.class).isEmpty(group.getAbout())) {
            groupAbout.setVisibility(View.GONE);
        } else {
            groupAbout.setVisibility(View.VISIBLE);
            groupAbout.setText(group.getAbout());
        }

        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupContact.class).query()
                .equal(GroupContact_.groupId, group.getId())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer(groupContacts -> {
                    $(GroupContactsHandler.class).setCurrentGroupContacts(groupContacts);
                    contactNames = new ArrayList<>();
                    for (GroupContact groupContact : groupContacts) {
                        contactNames.add($(NameHandler.class).getName(groupContact));
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

    private void redrawContacts() {
        List<String> names = new ArrayList<>();
        names.addAll(contactNames);
        names.addAll(contactInvites);

        if (names.isEmpty()) {
            peopleInGroup.setVisibility(View.GONE);
            peopleInGroup.setText(R.string.add_contact);
            return;
        }

        peopleInGroup.setVisibility(View.VISIBLE);

        peopleInGroup.setText(StringUtils.join(names, ", "));
    }

    private void setGroup(Group group) {
        this.group = group;

        if (groupDataSubscription != null) {
            $(DisposableHandler.class).dispose(groupDataSubscription);
        }

        if (group != null) {
            onGroupSet(group);
            groupChanged.onNext(group);
            setEventById(group.getEventId());
            setPhoneById(group.getPhoneId());
            $(RefreshHandler.class).refreshGroupMessages(group.getId());
            $(RefreshHandler.class).refreshGroupContacts(group.getId());

            groupDataSubscription = $(StoreHandler.class).getStore().box(Group.class).query()
                    .equal(Group_.id, group.getId())
                    .build()
                    .subscribe()
                    .onlyChanges()
                    .on(AndroidScheduler.mainThread())
                    .observer(groups -> {
                        if (groups.isEmpty()) return;
                        redrawContacts();
                        groupUpdated.onNext(groups.get(0));
                        $(RefreshHandler.class).refreshGroupContacts(group.getId());
                    });

            $(DisposableHandler.class).add(groupDataSubscription);
        }

        showGroupName(group);
    }

    public void showGroupName(Group group) {
        if (group == null) {
            groupName.setText(R.string.not_found);
            return;
        }

        if (group.hasPhone()) {
            $(DisposableHandler.class).add($(DataHandler.class).getPhone(group.getPhoneId()).subscribe(
                    phone -> {
                        groupName.setText(phone.getName());
                    }, error -> $(DefaultAlerts.class).thatDidntWork()
            ));
        } else {
            groupName.setText($(Val.class).of(group.getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)));
        }
    }

    public void setGroupBackground(Group group) {
        if (group.getPhoto() != null) {
            backgroundPhoto.setVisibility(View.VISIBLE);
            backgroundPhoto.setImageDrawable(null);
            $(PhotoLoader.class).softLoad(group.getPhoto(), backgroundPhoto);
        } else {
            backgroundPhoto.setVisibility(View.GONE);
        }
    }

    private void setEventById(String eventId) {
        if (eventId == null) {
            return;
        }

        $(DisposableHandler.class).add($(DataHandler.class).getEventById(eventId)
                .subscribe(event -> eventChanged.onNext(event),
                        error -> $(DefaultAlerts.class).thatDidntWork()));
    }

    private void setPhoneById(String phoneId) {
        if (phoneId == null) {
            return;
        }

        $(DisposableHandler.class).add($(DataHandler.class).getPhone(phoneId)
                .subscribe(phone -> phoneChanged.onNext(phone),
                        error -> $(DefaultAlerts.class).thatDidntWork()));
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

    public PublishSubject<Group> onGroupUpdated() {
        return groupUpdated;
    }

    public BehaviorSubject<Event> onEventChanged() {
        return eventChanged;
    }

    public BehaviorSubject<Phone> onPhoneChanged() {
        return phoneChanged;
    }
}
