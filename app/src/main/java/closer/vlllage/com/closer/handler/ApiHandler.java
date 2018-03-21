package closer.vlllage.com.closer.handler;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import closer.vlllage.com.closer.api.ApiService;
import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.SuccessResult;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.util.LatLngStr;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ApiHandler extends PoolMember {

    private final ApiService api = new ApiService();

    public void setAuthorization(String auth) {
        api.setAuthorization(auth);
    }

    public Observable<List<PhoneResult>> load(LatLng latLng) {
        return api.getBackend().near(LatLngStr.from(latLng))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SuccessResult> updatePhone(String latLng, String name, String status, Boolean active, String deviceToken) {
        return api.getBackend().phoneUpdate(latLng, name, status, active, deviceToken)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<PhoneResult> phone() {
        return api.getBackend().phone()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SuccessResult> sendMessage(String phone, String message) {
        return api.getBackend().send(phone, message)
                .observeOn(AndroidSchedulers.mainThread());
    }
}
