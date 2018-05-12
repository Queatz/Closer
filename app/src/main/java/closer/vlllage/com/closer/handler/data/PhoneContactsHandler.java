package closer.vlllage.com.closer.handler.data;

import java.util.List;

import closer.vlllage.com.closer.handler.group.PhoneContact;
import closer.vlllage.com.closer.handler.group.PhoneContacts;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.Observable;

public class PhoneContactsHandler extends PoolMember {

    private PhoneContacts phoneContacts;

    @Override
    protected void onPoolInit() {
        phoneContacts = $(ApplicationHandler.class).getApp().$(PhoneContacts.class);
    }

    public Observable<List<PhoneContact>> getAllContacts() {
        return phoneContacts.getAllContacts();
    }

    public void forceReload() {
        phoneContacts.forceReload();
    }
}
