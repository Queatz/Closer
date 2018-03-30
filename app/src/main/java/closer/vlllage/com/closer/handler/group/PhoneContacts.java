package closer.vlllage.com.closer.handler.group;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import closer.vlllage.com.closer.handler.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PhoneContacts extends PoolMember {

    private List<PhoneContact> contacts;

    public Observable<List<PhoneContact>> getAllContacts() {
        if (contacts != null && !contacts.isEmpty()) {
            return Observable.just(contacts);
        }

        return Observable.fromCallable(() -> {
            ContentResolver contentResolver = $(ApplicationHandler.class).getApp().getContentResolver();

            if (contentResolver == null) {
                return null;
            }

            try(Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)) {
                if (cursor == null) {
                    return null;
                }

                if (cursor.moveToFirst()) {
                    List<PhoneContact> contactsList = new ArrayList<>();

                    do {
                        String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactsList.add(new PhoneContact(contactName, contactNumber));
                    } while (cursor.moveToNext());

                    Collections.sort(contactsList, (c1, c2) -> c1.getName().compareTo(c2.getName()));
                    this.contacts = contactsList;
                    return contactsList;
                }
            }

            return null;
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }

    public void forceReload() {
        contacts = null;
    }
}
