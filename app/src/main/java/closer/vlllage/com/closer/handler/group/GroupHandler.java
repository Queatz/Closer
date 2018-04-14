package closer.vlllage.com.closer.handler.group;

import android.widget.TextView;

import org.greenrobot.essentials.StringUtils;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.DisposableHandler;
import closer.vlllage.com.closer.handler.PersistenceHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupInvite;
import closer.vlllage.com.closer.store.models.GroupInvite_;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;

public class GroupHandler extends PoolMember {

    private TextView groupName;
    private TextView peopleInGroup;
    private Group group;
    private GroupContact groupContact;
    private List<String> contactNames = new ArrayList<>();
    private List<String> contactInvites = new ArrayList<>();

    public void attach(TextView groupName, TextView peopleInGroup) {
        this.groupName = groupName;
        this.peopleInGroup = peopleInGroup;
    }

    public void setGroupById(String groupId) {
        if (groupId != null) {
            setGroup($(StoreHandler.class).getStore().box(Group.class).query()
                    .equal(Group_.id, groupId)
                    .build().findFirst());
            setGroupContact();
        }

        if (group != null) {
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
                            contactNames.add(getContactName(groupContact));
                        }

                        redrawContacts();
                    }));

            $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupInvite.class).query()
                    .equal(GroupInvite_.group, group.getId())
                    .build().subscribe().on(AndroidScheduler.mainThread())
                    .observer(groupInvites -> {
                        contactInvites = new ArrayList<>();
                        for (GroupInvite groupInvite : groupInvites) {
                            contactInvites.add($(ResourcesHandler.class).getResources().getString(R.string.contact_invited_inline, getInviteName(groupInvite)));
                        }
                        redrawContacts();
                    }));
        }
    }

    private void setGroupContact() {
        if (group == null) {
            return;
        }

        if ($(PersistenceHandler.class).getPhoneId() == null) {
            return;
        }

        groupContact = $(StoreHandler.class).getStore().box(GroupContact.class).query()
                .equal(GroupContact_.groupId, group.getId())
                .equal(GroupContact_.contactId, $(PersistenceHandler.class).getPhoneId())
                .build().findFirst();
    }

    private String getContactName(GroupContact groupContact) {
        if (groupContact.getContactName() == null || groupContact.getContactName().trim().isEmpty()) {
            return $(ResourcesHandler.class).getResources().getString(R.string.no_name);
        }

        return groupContact.getContactName();
    }

    private String getInviteName(GroupInvite groupInvite) {
        if (groupInvite.getName() == null || groupInvite.getName().trim().isEmpty()) {
            return $(ResourcesHandler.class).getResources().getString(R.string.no_name);
        }

        return groupInvite.getName();
    }

    private void redrawContacts() {
        List<String> names = new ArrayList<>();

        names.addAll(contactNames);
        names.addAll(contactInvites);

        if (names.isEmpty()) {
            peopleInGroup.setText(R.string.add_contact);
            return;
        }


        peopleInGroup.setText(StringUtils.join(names, ", "));
    }

    private void setGroup(Group group) {
        this.group = group;

        if (group != null && group.getName() != null) {
            groupName.setText(group.getName());
        } else {
            groupName.setText(R.string.not_found);
        }
    }

    public Group getGroup() {
        return group;
    }

    public GroupContact getGroupContact() {
        return groupContact;
    }
}
