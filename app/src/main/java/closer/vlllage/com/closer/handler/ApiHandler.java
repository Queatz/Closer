package closer.vlllage.com.closer.handler;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import closer.vlllage.com.closer.api.ApiService;
import closer.vlllage.com.closer.api.models.CreateResult;
import closer.vlllage.com.closer.api.models.GroupMessageResult;
import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.StateResult;
import closer.vlllage.com.closer.api.models.SuccessResult;
import closer.vlllage.com.closer.api.models.SuggestionResult;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.util.LatLngStr;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ApiHandler extends PoolMember {

    private final ApiService api = new ApiService();

    public void setAuthorization(String auth) {
        api.setAuthorization(auth);
    }

    public Observable<List<PhoneResult>> getPhonesNear(LatLng latLng) {
        return api.getBackend().getPhonesNear(LatLngStr.from(latLng))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<SuggestionResult>> getSuggestionsNear(LatLng latLng) {
        return api.getBackend().getSuggestionsNear(LatLngStr.from(latLng))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<CreateResult> addSuggestion(String name, LatLng latLng) {
        return api.getBackend().addSuggestion(name, LatLngStr.from(latLng))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<CreateResult> updatePhone(String latLng, String name, String status, Boolean active, String deviceToken) {
        return api.getBackend().phoneUpdate(latLng, name, status, active, deviceToken)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<PhoneResult> phone() {
        return api.getBackend().phone()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SuccessResult> sendMessage(String phone, String message) {
        return api.getBackend().sendMessage(phone, message)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> isVerified() {
        return api.getBackend().getIsVerified()
                .observeOn(AndroidSchedulers.mainThread())
                .map(verifiedResult -> verifiedResult.verified);
    }

    public Observable<List<GroupMessageResult>> myMessages() {
        return api.getBackend().myMessages()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<StateResult> myGroups(LatLng latLng) {
        return api.getBackend().myGroups(LatLngStr.from(latLng))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SuccessResult> setPhoneNumber(String phoneNumber) {
        return api.getBackend().setPhoneNumber(phoneNumber)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SuccessResult> sendVerificationCode(String verificationCode) {
        return api.getBackend().sendVerificationCode(verificationCode)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<CreateResult> sendGroupMessage(String groupId, String text, String attachment) {
        return api.getBackend().sendGroupMessage(groupId, text, attachment)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<CreateResult> createGroup(String groupName) {
        return api.getBackend().createGroup(groupName)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<CreateResult> createPublicGroup(String groupName, String about, LatLng latLng) {
        return api.getBackend().createPublicGroup(groupName, about, LatLngStr.from(latLng), true)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SuccessResult> inviteToGroup(String groupId, String name, String phoneNumber) {
        return api.getBackend().inviteToGroup(groupId, name, phoneNumber)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SuccessResult> leaveGroup(String groupId) {
        return api.getBackend().leaveGroup(groupId, true)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SuccessResult> cancelInvite(String groupId, String groupInviteId) {
        return api.getBackend().cancelInvite(groupId, groupInviteId)
                .observeOn(AndroidSchedulers.mainThread());
    }
}
