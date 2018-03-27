package closer.vlllage.com.closer.handler;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.handler.group.PhoneContact;
import closer.vlllage.com.closer.pool.PoolMember;

public class PhoneContactsHandler extends PoolMember {

    private List<PhoneContact> contacts;

    @Nullable
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    public List<PhoneContact> getAllContacts() {
        if (contacts != null && !contacts.isEmpty()) {
            return contacts;
        }

        ContentResolver contentResolver = $(ApplicationHandler.class).getApp().getContentResolver();

        if (contentResolver == null) {
            return null;
        }

        try(Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)) {
            if (cursor == null) {
                return null;
            }

            if (cursor.moveToFirst()) {
                List<PhoneContact> contactsList = new ArrayList<>();

                do {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        try(Cursor contactCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null)) {
                            if (contactCursor != null) {
                                contactCursor.moveToNext();
                                String contactName = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String contactNumber = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                contactsList.add(new PhoneContact(contactName, contactNumber));
                            }
                        }
                    }

                } while (cursor.moveToNext());

                this.contacts = contactsList;
                return contactsList;
            }
        }

        return null;
    }

    public void forceReload() {
        contacts = null;
    }
}
