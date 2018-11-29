package closer.vlllage.com.closer.handler.data;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import closer.vlllage.com.closer.api.ApiService;
import closer.vlllage.com.closer.api.models.CreateResult;
import closer.vlllage.com.closer.api.models.EventResult;
import closer.vlllage.com.closer.api.models.GroupActionResult;
import closer.vlllage.com.closer.api.models.GroupMemberResult;
import closer.vlllage.com.closer.api.models.GroupMessageResult;
import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.StateResult;
import closer.vlllage.com.closer.api.models.SuccessResult;
import closer.vlllage.com.closer.api.models.SuggestionResult;
import closer.vlllage.com.closer.handler.helpers.DateFormatter;
import closer.vlllage.com.closer.handler.helpers.HttpEncode;
import closer.vlllage.com.closer.handler.helpers.LatLngStr;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class ApiHandler extends PoolMember {

    private final ApiService api = new ApiService();

    public void setAuthorization(String auth) {
        api.setAuthorization(auth);
    }

    public Observable<List<PhoneResult>> getPhonesNear(LatLng latLng) {
        return uiThread(api.getBackend().getPhonesNear($(LatLngStr.class).from(latLng)));
    }

    public Observable<List<SuggestionResult>> getSuggestionsNear(LatLng latLng) {
        return uiThread(api.getBackend().getSuggestionsNear($(LatLngStr.class).from(latLng)));
    }

    public Observable<CreateResult> addSuggestion(String name, LatLng latLng) {
        return uiThread(api.getBackend().addSuggestion(name, $(LatLngStr.class).from(latLng)));
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
        return uiThread(api.getBackend().myMessages($(LatLngStr.class).from(latLng)));
    }

    public Observable<StateResult> myGroups(LatLng latLng) {
        return uiThread(api.getBackend().myGroups($(LatLngStr.class).from(latLng)));
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

    public Observable<CreateResult> sendAreaMessage(LatLng latLng, String text, String attachment) {
        return uiThread(api.getBackend().sendAreaMessage($(LatLngStr.class).from(latLng), text, attachment));
    }

    public Observable<CreateResult> createGroup(String groupName) {
        return uiThread(api.getBackend().createGroup(groupName));
    }

    public Observable<CreateResult> createPublicGroup(String groupName, String about, LatLng latLng) {
        return uiThread(api.getBackend().createPublicGroup(groupName, about, $(LatLngStr.class).from(latLng), true));
    }

    public Observable<CreateResult> createPhysicalGroup(LatLng latLng) {
        return uiThread(api.getBackend().createPhysicalGroup($(LatLngStr.class).from(latLng), true));
    }

    public Observable<SuccessResult> convertToHub(String groupId, String name) {
        return uiThread(api.getBackend().convertToHub(groupId, name, true));
    }

    public Observable<SuccessResult> inviteToGroup(String groupId, String name, String phoneNumber) {
        return uiThread(api.getBackend().inviteToGroup(groupId, name, phoneNumber));
    }

    public Observable<SuccessResult> leaveGroup(String groupId) {
        return uiThread(api.getBackend().leaveGroup(groupId, true));
    }

    public Observable<SuccessResult> setGroupPhoto(String groupId, String photo) {
        return uiThread(api.getBackend().setGroupPhoto(groupId, photo));
    }

    public Observable<SuccessResult> removeGroupAction(String groupActionId) {
        return uiThread(api.getBackend().removeGroupAction(groupActionId));
    }

    public Observable<CreateResult> createGroupAction(String groupId, String name, String intent) {
        return uiThread(api.getBackend().createGroupAction(groupId, name, intent));
    }

    public Observable<List<GroupActionResult>> getGroupActions(String groupId) {
        return uiThread(api.getBackend().getGroupActions(groupId));
    }

    public Observable<List<GroupActionResult>> getGroupActions(LatLng latLng) {
        return uiThread(api.getBackend().getGroupActionsNearGeo($(LatLngStr.class).from(latLng)));
    }

    public Observable<SuccessResult> setGroupActionPhoto(String groupActionId, String photo) {
        return uiThread(api.getBackend().setGroupActionPhoto(groupActionId, photo));
    }

    public Observable<List<GroupMemberResult>> getAllGroupMember() {
        return uiThread(api.getBackend().getAllGroupMembers());
    }

    public Observable<GroupMemberResult> getGroupMember(String groupId) {
        return uiThread(api.getBackend().getGroupMember(groupId));
    }

    public Observable<CreateResult> updateGroupMember(String groupId, boolean muted, boolean subscribed) {
        return uiThread(api.getBackend().updateGroupMember(groupId, muted, subscribed));
    }

    public Observable<SuccessResult> cancelInvite(String groupId, String groupInviteId) {
        return uiThread(api.getBackend().cancelInvite(groupId, groupInviteId));
    }

    public Observable<CreateResult> createEvent(String name, String about, boolean isPublic, LatLng latLng, Date startsAt, Date endsAt) {
        return uiThread(api.getBackend().createEvent(name, about, isPublic, $(LatLngStr.class).from(latLng), $(HttpEncode.class).encode($(DateFormatter.class).format(startsAt)), $(HttpEncode.class).encode($(DateFormatter.class).format(endsAt))));
    }

    public Observable<List<EventResult>> getEvents(LatLng latLng) {
        return uiThread(api.getBackend().getEvents($(LatLngStr.class).from(latLng)));
    }

    public Observable<EventResult> getEvent(String eventId) {
        return uiThread(api.getBackend().getEvent(eventId));
    }

    public Observable<SuccessResult> cancelEvent(String eventId) {
        return uiThread(api.getBackend().cancelEvent(eventId, true));
    }

    public Observable<List<GroupResult>> getPhysicalGroups(LatLng latLng) {
        return uiThread(api.getBackend().getPhysicalGroups($(LatLngStr.class).from(latLng), "physical"));
    }

    public Observable<String> uploadPhoto(InputStream photo) {
        RequestBody body = new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return MediaType.parse("image/*");
            }

            @Override
            public long contentLength() {
                try {
                    return photo.available();
                } catch (IOException e) {
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            public void writeTo(BufferedSink sink) {
                Source source = Okio.source(photo);
                try {
                    sink.writeAll(source);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", "closer-photo", body);
        String id = $(Val.class).rndId();
        return uiThread(api.getPhotoUploadBackend().uploadPhoto(id, part))
                .map(responseBody -> id);
    }

    public Observable<String> privacy() {
        return uiThread(api.getContentBackend().privacy()).map(ResponseBody::string);
    }

    private <T> Observable<T> uiThread(Observable<T> observable) {
        return observable.observeOn(AndroidSchedulers.mainThread());
    }
}
