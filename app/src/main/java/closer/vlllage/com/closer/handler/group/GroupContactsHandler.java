package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.AlertHandler;
import closer.vlllage.com.closer.handler.ApiHandler;
import closer.vlllage.com.closer.handler.DefaultAlerts;
import closer.vlllage.com.closer.handler.DisposableHandler;
import closer.vlllage.com.closer.handler.PermissionHandler;
import closer.vlllage.com.closer.handler.PhoneContactsHandler;
import closer.vlllage.com.closer.handler.RefreshHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Group;

import static android.Manifest.permission.READ_CONTACTS;

public class GroupContactsHandler extends PoolMember {

    private RecyclerView contactsRecyclerView;
    private EditText searchContacts;
    private PhoneContactAdapter phoneContactAdapter;

    @SuppressWarnings("MissingPermission")
    public void attach(Group group, RecyclerView contactsRecyclerView, EditText searchContacts) {
        this.contactsRecyclerView = contactsRecyclerView;
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
    }

    private void inviteToGroup(Group group, PhoneContact phoneContact) {
        $(DisposableHandler.class).add($(ApiHandler.class).inviteToGroup(group.getId(),
                phoneContact.getName(),
                phoneContact.getPhoneNumber()
        ).subscribe(
                successResult -> {
                    if (successResult.success) {
                        $(AlertHandler.class).make()
                                .setMessage($(ResourcesHandler.class).getResources().getString(R.string.phone_invited, phoneContact.getName(), group.getName()))
                                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.yaay))
                                .show();
                        $(RefreshHandler.class).refreshMyGroups();
                    } else {
                        $(DefaultAlerts.class).thatDidntWork();
                    }
                }, error -> $(DefaultAlerts.class).thatDidntWork()));
    }

    public boolean isEmpty() {
        return phoneContactAdapter.getItemCount() == 0;
    }

    @SuppressWarnings("MissingPermission")
    public void showContactsForQuery(String originalQuery) {
        String phoneNumber = $(PhoneNumberHandler.class).normalize(originalQuery);

        phoneContactAdapter.setPhoneNumber(phoneNumber);

        if(!$(PermissionHandler.class).has(READ_CONTACTS)) {
            return;
        }

        $(DisposableHandler.class).add($(PhoneContactsHandler.class).getAllContacts().subscribe(allContacts -> {
            String query = originalQuery.trim().toLowerCase();

            if (allContacts == null) {
                return;
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
        }));
    }

    public void showContactsForQuery() {
        showContactsForQuery(searchContacts.getText().toString());
    }
}
