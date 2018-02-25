package closer.vlllage.com.closer.handler;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class AccountHandler extends PoolMember {

    public static final String ACCOUNT_FIELD_STATUS = "status";
    public static final String ACCOUNT_FIELD_NAME = "name";
    public static final String ACCOUNT_FIELD_GEO = "geo";
    public static final String ACCOUNT_FIELD_ACTIVE = "active";

    private final PublishSubject<AccountChange> accountChanges = PublishSubject.create();

    public void updateGeo(LatLng latLng) {
        accountChanges.onNext(new AccountChange(ACCOUNT_FIELD_GEO, latLng));
    }

    public void updateName(String name) {
        $(PersistenceHandler.class).setMyName(name);
        accountChanges.onNext(new AccountChange(ACCOUNT_FIELD_NAME, name));
    }

    public void updateStatus(String status) {
        $(PersistenceHandler.class).setMyStatus(status);
        accountChanges.onNext(new AccountChange(ACCOUNT_FIELD_STATUS, status));
    }

    public void updateActive(boolean active) {
        $(PersistenceHandler.class).setMyActive(active);
        accountChanges.onNext(new AccountChange(ACCOUNT_FIELD_ACTIVE, active));
    }

    public String getName() {
        return $(PersistenceHandler.class).getMyName();
    }

    public String getStatus() {
        return $(PersistenceHandler.class).getMyStatus();
    }

    public boolean getActive() {
        return $(PersistenceHandler.class).getMyActive();
    }

    public Observable<AccountChange> changes() {
        return accountChanges;
    }

    public void updateDeviceToken(String deviceToken) {
        $(PersistenceHandler.class).setDeviceToken(deviceToken);
        $(ApiHandler.class).updatePhone(deviceToken);
    }

    public static class AccountChange {

        public final String prop;
        public final Object value;

        public AccountChange(String prop, Object value) {
            this.prop = prop;
            this.value = value;
        }
    }
}
