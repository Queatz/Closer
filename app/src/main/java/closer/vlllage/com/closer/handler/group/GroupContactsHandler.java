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
import closer.vlllage.com.closer.handler.PermissionHandler;
import closer.vlllage.com.closer.handler.PhoneContactsHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;

import static android.Manifest.permission.READ_CONTACTS;

public class GroupContactsHandler extends PoolMember {

    private RecyclerView contactsRecyclerView;
    private EditText searchContacts;
    private PhoneContactAdapter phoneContactAdapter;

    @SuppressWarnings("MissingPermission")
    public void attach(RecyclerView contactsRecyclerView, EditText searchContacts) {
        this.contactsRecyclerView = contactsRecyclerView;
        this.searchContacts = searchContacts;
        phoneContactAdapter = new PhoneContactAdapter(this, phoneContact -> {
            $(AlertHandler.class).makeAlert()
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.add_to_group))
                    .setTitle($(ResourcesHandler.class).getResources().getString(R.string.add_to_group))
                    .show();
        });

        if($(PermissionHandler.class).has(READ_CONTACTS)) {
            phoneContactAdapter.setContacts($(PhoneContactsHandler.class).getAllContacts());
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

    public boolean isEmpty() {
        return phoneContactAdapter.getItemCount() == 0;
    }

    @SuppressWarnings("MissingPermission")
    public void showContactsForQuery(String query) {
        if(!$(PermissionHandler.class).has(READ_CONTACTS)) {
            return;
        }

        query = query.trim().toLowerCase();

        List<PhoneContact> allContacts = $(PhoneContactsHandler.class).getAllContacts();

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
    }

    public void showContactsForQuery() {
        showContactsForQuery(searchContacts.getText().toString());
    }
}
