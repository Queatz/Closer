package closer.vlllage.com.closer.handler;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.api.ApiService;
import closer.vlllage.com.closer.api.models.CreateResult;
import closer.vlllage.com.closer.api.models.EventResult;
import closer.vlllage.com.closer.api.models.GroupMessageResult;
import closer.vlllage.com.closer.api.models.GroupResult;
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
        return uiThread(api.getBackend().getPhonesNear(LatLngStr.from(latLng)));
    }

    public Observable<List<SuggestionResult>> getSuggestionsNear(LatLng latLng) {
        return uiThread(api.getBackend().getSuggestionsNear(LatLngStr.from(latLng)));
    }

    public Observable<CreateResult> addSuggestion(String name, LatLng latLng) {
        return uiThread(api.getBackend().addSuggestion(name, LatLngStr.from(latLng)));
    }

    public Observable<CreateResult> updatePhone(String latLng, String name, String status, Boolean active, String deviceToken) {
        return uiThread(api.getBackend().phoneUpdate(latLng, name, status, active, deviceToken));
    }

    public Observable<PhoneResult> phone() {
        return uiThread(api.getBackend().phone());
    }

    public Observable<SuccessResult> sendMessage(String phone, String message) {
        return uiThread(api.getBackend().sendMessage(phone, message));
    }

    public Observable<Boolean> isVerified() {
        return uiThread(api.getBackend().getIsVerified())
                .map(verifiedResult -> verifiedResult.verified);
    }

    public Observable<List<GroupMessageResult>> myMessages(LatLng latLng) {
        return uiThread(api.getBackend().myMessages(LatLngStr.from(latLng)));
    }

    public Observable<StateResult> myGroups(LatLng latLng) {
        return uiThread(api.getBackend().myGroups(LatLngStr.from(latLng)));
    }

    public Observable<GroupResult> getGroup(String groupId) {
        return uiThread(api.getBackend().getGroup(groupId));
    }

    public Observable<SuccessResult> setPhoneNumber(String phoneNumber) {
        return uiThread(api.getBackend().setPhoneNumber(phoneNumber));
    }

    public Observable<SuccessResult> sendVerificationCode(String verificationCode) {
        return uiThread(api.getBackend().sendVerificationCode(verificationCode));
    }

    public Observable<CreateResult> sendGroupMessage(String groupId, String text, String attachment) {
        return uiThread(api.getBackend().sendGroupMessage(groupId, text, attachment));
    }

    public Observable<CreateResult> createGroup(String groupName) {
        return uiThread(api.getBackend().createGroup(groupName));
    }

    public Observable<CreateResult> createPublicGroup(String groupName, String about, LatLng latLng) {
        return uiThread(api.getBackend().createPublicGroup(groupName, about, LatLngStr.from(latLng), true));
    }

    public Observable<SuccessResult> inviteToGroup(String groupId, String name, String phoneNumber) {
        return uiThread(api.getBackend().inviteToGroup(groupId, name, phoneNumber));
    }

    public Observable<SuccessResult> leaveGroup(String groupId) {
        return uiThread(api.getBackend().leaveGroup(groupId, true));
    }

    public Observable<SuccessResult> cancelInvite(String groupId, String groupInviteId) {
        return uiThread(api.getBackend().cancelInvite(groupId, groupInviteId));
    }

    public Observable<CreateResult> createEvent(String name, String about, LatLng latLng, Date startsAt, Date endsAt) {
        return uiThread(api.getBackend().createEvent(name, about, LatLngStr.from(latLng), $(HttpEncode.class).encode($(DateFormatter.class).format(startsAt)), $(HttpEncode.class).encode($(DateFormatter.class).format(endsAt))));
    }

    public Observable<List<EventResult>> getEvents(LatLng latLng) {
        return uiThread(api.getBackend().getEvents(LatLngStr.from(latLng)));
    }

    public Observable<EventResult> getEvent(String eventId) {
        return uiThread(api.getBackend().getEvent(eventId));
    }

    public Observable<SuccessResult> cancelEvent(String eventId) {
        return uiThread(api.getBackend().cancelEvent(eventId, true));
    }

    private <T> Observable<T> uiThread(Observable<T> observable) {
        return observable.observeOn(AndroidSchedulers.mainThread());
    }
}
