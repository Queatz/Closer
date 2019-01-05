package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.SuccessResult;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.LocationHandler;
import closer.vlllage.com.closer.handler.data.PermissionHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.PhoneContactsHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.MenuHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.TimeAgo;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.map.SetNameHandler;
import closer.vlllage.com.closer.handler.phone.NameHandler;
import closer.vlllage.com.closer.handler.phone.PhoneMessagesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupInvite;
import closer.vlllage.com.closer.store.models.GroupInvite_;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.DataSubscription;
import io.reactivex.Single;

import static android.Manifest.permission.READ_CONTACTS;

public class GroupContactsHandler extends PoolMember {

    private RecyclerView contactsRecyclerView;
    private View showPhoneContactsButton;
    private EditText searchContacts;
    private PhoneContactAdapter phoneContactAdapter;
    private final Set<String> currentGroupContacts = new HashSet<>();
    private DataSubscription dataSubscription;

    @SuppressWarnings("MissingPermission")
    public void attach(Group group, RecyclerView contactsRecyclerView, EditText searchContacts, View showPhoneContactsButton) {
        this.contactsRecyclerView = contactsRecyclerView;
        this.showPhoneContactsButton = showPhoneContactsButton;
        this.searchContacts = searchContacts;
        phoneContactAdapter = new PhoneContactAdapter(this, phoneContact -> {
            if (phoneContact.getName() == null) {
                $(AlertHandler.class).make()
                        .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.invite))
                        .setLayoutResId(R.layout.invite_by_number_modal)
                        .setTextView(R.id.input, name -> {
                            phoneContact.setName(name);
                            inviteToGroup(group, phoneContact);
                        })
                        .setTitle($(ResourcesHandler.class).getResources().getString(R.string.invite_to_group, group.getName()))
                        .show();
            } else {
                $(AlertHandler.class).make()
                        .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.add_phone_name, phoneContact.getFirstName()))
                        .setMessage(phoneContact.getPhoneNumber())
                        .setPositiveButtonCallback(alertResult -> inviteToGroup(group, phoneContact))
                        .setTitle($(ResourcesHandler.class).getResources().getString(R.string.add_phone_to_group, phoneContact.getFirstName(), group.getName()))
                        .show();
            }
        }, groupInvite -> {
            $(AlertHandler.class).make()
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.cancel_invite))
                    .setMessage($(ResourcesHandler.class).getResources().getString(R.string.confirm_cancel_invite, groupInvite.getName()))
                    .setPositiveButtonCallback(alertResult -> {
                        cancelInvite(groupInvite);
                    })
                    .show();
        }, groupContact -> {
            if ($(PersistenceHandler.class).getPhoneId().equals(groupContact.getContactId())) {
                $(MenuHandler.class).show(new MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.leave_group_action, () -> {
                    $(AlertHandler.class).make()
                            .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.leave_group, group.getName()))
                            .setPositiveButtonCallback(result -> leaveGroup(group))
                            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.leave_group_title, group.getName()))
                            .setMessage($(ResourcesHandler.class).getResources().getString(R.string.leave_group_message))
                            .show();
                }));
            } else {
                $(PhoneMessagesHandler.class).openMessagesWithPhone(groupContact.getContactId(), groupContact.getContactName(), "");
            }
        });

        if($(PermissionHandler.class).has(READ_CONTACTS)) {
            $(DisposableHandler.class).add(
                    $(PhoneContactsHandler.class).getAllContacts().subscribe(phoneContactAdapter::setContacts)
            );
        }

        contactsRecyclerView.setAdapter(phoneContactAdapter);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(
                contactsRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        ));

        searchContacts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                showContactsForQuery(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupInvite.class).query()
                .equal(GroupInvite_.group, group.getId())
                .build().subscribe().on(AndroidScheduler.mainThread()).observer(groupInvites -> {
                    phoneContactAdapter.setInvites(groupInvites);
                }));

        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupContact.class).query()
                .equal(GroupContact_.groupId, group.getId())
                .build().subscribe().on(AndroidScheduler.mainThread()).observer(groupContacts -> {
                    phoneContactAdapter.setGroupContacts(groupContacts);
                }));
    }

    private void leaveGroup(Group group) {
        $(DisposableHandler.class).add($(ApiHandler.class).leaveGroup(group.getId()).subscribe(successResult -> {
            if (successResult.success) {
                $(DefaultAlerts.class).message(
                        $(ResourcesHandler.class).getResources().getString(R.string.group_no_more, group.getName()),
                        ignored -> $(ActivityHandler.class).getActivity().finish());
                $(RefreshHandler.class).refreshMyGroups();
            } else {
                $(DefaultAlerts.class).thatDidntWork();
            }
        }, error -> $(DefaultAlerts.class).thatDidntWork()));
    }

    private void cancelInvite(GroupInvite groupInvite) {
        $(DisposableHandler.class).add($(ApiHandler.class).cancelInvite(groupInvite.getGroup(), groupInvite.getId()).subscribe(successResult -> {
            if (successResult.success) {
                $(AlertHandler.class).make()
                        .setMessage($(ResourcesHandler.class).getResources().getString(R.string.invite_cancelled))
                        .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                        .show();
                $(RefreshHandler.class).refreshMyGroups();
            } else {
                $(DefaultAlerts.class).thatDidntWork(successResult.error);
            }
        }));
    }

    private void inviteToGroup(Group group, PhoneContact phoneContact) {
        String myName = $(AccountHandler.class).getName();
        if (myName == null || myName.trim().isEmpty()) {
            $(SetNameHandler.class).modifyName(name -> sendInviteToGroup(group, phoneContact), true);
        } else {
            sendInviteToGroup(group, phoneContact);
        }
    }

    private void sendInviteToGroup(Group group, PhoneContact phoneContact) {
        Single<SuccessResult> inviteToGroup = phoneContact.getPhoneId() == null ?
                $(ApiHandler.class).inviteToGroup(group.getId(), phoneContact.getName(), phoneContact.getPhoneNumber()) :
                $(ApiHandler.class).inviteToGroup(group.getId(), phoneContact.getPhoneId());

        $(DisposableHandler.class).add(inviteToGroup.subscribe(successResult -> {
                    if (successResult.success) {
                        String message = phoneContact.getName() == null || phoneContact.getName().trim().isEmpty() ?
                                $(ResourcesHandler.class).getResources().getString(R.string.phone_invited, phoneContact.getPhoneNumber(), group.getName()) :
                                $(ResourcesHandler.class).getResources().getString(R.string.phone_invited, phoneContact.getName(), group.getName());
                        $(AlertHandler.class).make()
                                .setMessage(message)
                                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.yaay))
                                .show();
                        $(RefreshHandler.class).refreshMyGroups();
                    } else {
                        $(DefaultAlerts.class).thatDidntWork(successResult.error);
                    }
                }, error -> $(DefaultAlerts.class).thatDidntWork()));
    }

    public boolean isEmpty() {
        return phoneContactAdapter.getItemCount() == 0;
    }

    @SuppressWarnings("MissingPermission")
    public void showContactsForQuery(String originalQuery) {
        String query = originalQuery.trim().toLowerCase();

        if ($(LocationHandler.class).getLastKnownLocation() != null) {
            $(DisposableHandler.class).add($(ApiHandler.class).searchPhonesNear(new LatLng(
                    $(LocationHandler.class).getLastKnownLocation().getLatitude(),
                    $(LocationHandler.class).getLastKnownLocation().getLongitude()
            ), query).subscribe(phoneResults -> {
                    for (PhoneResult phoneResult : phoneResults) {
                        $(RefreshHandler.class).refresh(PhoneResult.from(phoneResult));
                    }
            }, error -> $(DefaultAlerts.class).thatDidntWork()));
        }

        String phoneNumber = $(PhoneNumberHandler.class).normalize(originalQuery);

        phoneContactAdapter.setPhoneNumber(phoneNumber);
        phoneContactAdapter.setIsFiltered(!originalQuery.isEmpty());

        if(!$(PermissionHandler.class).has(READ_CONTACTS)) {
            showPhoneContacts(new ArrayList<>(), query);
            return;
        }

        $(DisposableHandler.class).add($(PhoneContactsHandler.class).getAllContacts().subscribe(phoneContacts -> {
            showPhoneContacts(phoneContacts, query);
        }, error -> $(DefaultAlerts.class).thatDidntWork()));
    }

    private void showPhoneContacts(List<PhoneContact> phoneContacts, String query) {
        if (dataSubscription != null) {
            $(DisposableHandler.class).dispose(dataSubscription);
        }
        dataSubscription = $(StoreHandler.class).getStore().box(Phone.class).query()
                .contains(Phone_.name, $(Val.class).of(query))
                .notNull(Phone_.id)
                .greater(Phone_.updated, $(TimeAgo.class).fifteenDaysAgo())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer(closerContacts -> {
                    List<PhoneContact> allContacts = new ArrayList<>();

                    if (phoneContacts != null) {
                        allContacts.addAll(phoneContacts);
                    }

                    for (Phone phone : closerContacts) {
                        if (currentGroupContacts.contains(phone.getId())) {
                            continue;
                        }

                        allContacts.add(0, new PhoneContact($(NameHandler.class).getName(phone), phone.getStatus()).setPhoneId(phone.getId()));
                    }

                    if (query.isEmpty()) {
                        phoneContactAdapter.setContacts(allContacts);
                        return;
                    }

                    String queryPhone = query.replaceAll("[^0-9]", "");

                    List<PhoneContact> contacts = new ArrayList<>();
                    for(PhoneContact contact : allContacts) {
                        if (contact.getName() != null) {
                            if (contact.getName().toLowerCase().contains(query)) {
                                contacts.add(contact);
                                continue;
                            }
                        }

                        if (!queryPhone.isEmpty() && contact.getPhoneNumber() != null) {
                            if (contact.getPhoneNumber().replaceAll("[^0-9]", "").contains(queryPhone)) {
                                contacts.add(contact);
                            }
                        }
                    }

                    phoneContactAdapter.setContacts(contacts);
                });
        $(DisposableHandler.class).add(dataSubscription);
    }

    public void showContactsForQuery() {
        showContactsForQuery(searchContacts.getText().toString());
    }

    public void setCurrentGroupContacts(List<GroupContact> groupContacts) {
        currentGroupContacts.clear();
        for (GroupContact groupContact : groupContacts) {
            currentGroupContacts.add(groupContact.getContactId());
        }
    }
}
