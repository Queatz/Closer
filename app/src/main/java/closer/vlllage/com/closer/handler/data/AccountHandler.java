package closer.vlllage.com.closer.handler.data;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.LatLngStr;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class AccountHandler extends PoolMember {

    public static final String ACCOUNT_FIELD_STATUS = "status";
    public static final String ACCOUNT_FIELD_NAME = "name";
    public static final String ACCOUNT_FIELD_GEO = "geo";
    public static final String ACCOUNT_FIELD_ACTIVE = "active";

    private final static PublishSubject<AccountChange> accountChanges = PublishSubject.create();

    public void updateGeo(LatLng latLng) {
        accountChanges.onNext(new AccountChange(ACCOUNT_FIELD_GEO, latLng));

        $(DisposableHandler.class).add($(ApiHandler.class).updatePhone($(LatLngStr.class).from(latLng), null, null, null, null)
            .subscribe(success -> {}, this::onError));
    }

    public void updateName(String name) {
        $(PersistenceHandler.class).setMyName(name);
        accountChanges.onNext(new AccountChange(ACCOUNT_FIELD_NAME, name));
        $(DisposableHandler.class).add($(ApiHandler.class).updatePhone(null, name, null, null, null)
            .subscribe(success -> {}, this::onError));

    }

    public void updateStatus(String status) {
        $(PersistenceHandler.class).setMyStatus(status);
        accountChanges.onNext(new AccountChange(ACCOUNT_FIELD_STATUS, status));
        $(DisposableHandler.class).add($(ApiHandler.class).updatePhone(null, null, status, null, null)
            .subscribe(success -> {}, this::onError));
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();
        $(ConnectionErrorHandler.class).notifyConnectionError();
    }

    public void updateActive(boolean active) {
        $(PersistenceHandler.class).setMyActive(active);
        accountChanges.onNext(new AccountChange(ACCOUNT_FIELD_ACTIVE, active));
        $(DisposableHandler.class).add($(ApiHandler.class).updatePhone(null, null, null, active, null)
            .subscribe(success -> {}, this::onError));

        if (!active) {
            return;
        }

        $(LocationHandler.class).getCurrentLocation(location -> updateGeo(new LatLng(location.getLatitude(), location.getLongitude())));
    }

    public void updateDeviceToken(String deviceToken) {
        $(PersistenceHandler.class).setDeviceToken(deviceToken);
        $(DisposableHandler.class).add($(ApiHandler.class).updatePhone(null, null, null, null, deviceToken)
            .subscribe(createResult -> {
                $(PersistenceHandler.class).setPhoneId(createResult.id);
            }, this::onError));
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

    public String getPhone() {
        String phone = $(PersistenceHandler.class).getPhone();

        if (phone == null) {
            phone = $(Val.class).rndId();
            $(PersistenceHandler.class).setPhone(phone);
        }

        return phone;
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
