package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import closer.vlllage.com.closer.handler.PhoneContactsHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class GroupContactsHandler extends PoolMember {

    private RecyclerView contactsRecyclerView;
    private EditText searchContacts;
    private PhoneContactAdapter phoneContactAdapter;

    public void attach(RecyclerView contactsRecyclerView, EditText searchContacts) {
        this.contactsRecyclerView = contactsRecyclerView;
        this.searchContacts = searchContacts;
        phoneContactAdapter = new PhoneContactAdapter(this, $(PhoneContactsHandler.class).getAllContacts());

        contactsRecyclerView.setAdapter(phoneContactAdapter);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(
                contactsRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        ));
    }

    public boolean isEmpty() {
        return phoneContactAdapter.getItemCount() == 0;
    }

    public void showContactsForQuery(String query) {

    }
}
