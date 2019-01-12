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
import closer.vlllage.com.closer.api.models.ReactionResult;
import closer.vlllage.com.closer.api.models.StateResult;
import closer.vlllage.com.closer.api.models.SuccessResult;
import closer.vlllage.com.closer.api.models.SuggestionResult;
import closer.vlllage.com.closer.handler.helpers.DateFormatter;
import closer.vlllage.com.closer.handler.helpers.HttpEncode;
import closer.vlllage.com.closer.handler.helpers.LatLngStr;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.Single;
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

    public Single<List<PhoneResult>> getPhonesNear(LatLng latLng) {
        return uiThread(api.getBackend().getPhonesNear($(LatLngStr.class).from(latLng)));
    }

    public Single<List<SuggestionResult>> getSuggestionsNear(LatLng latLng) {
        return uiThread(api.getBackend().getSuggestionsNear($(LatLngStr.class).from(latLng)));
    }

    public Single<CreateResult> addSuggestion(String name, LatLng latLng) {
        return uiThread(api.getBackend().addSuggestion(name, $(LatLngStr.class).from(latLng)));
    }

    public Single<CreateResult> updatePhone(String latLng, String name, String status, Boolean active, String deviceToken) {
        return uiThread(api.getBackend().phoneUpdate(latLng, name, status, active, deviceToken));
    }

    public Single<CreateResult> updatePhonePhoto(String photoUrl) {
        return uiThread(api.getBackend().phoneUpdatePhoto(photoUrl));
    }

    public Single<CreateResult> updatePhonePrivateMode(boolean privateMode) {
        return uiThread(api.getBackend().updatePhonePrivateMode(privateMode));
    }

    public Single<PhoneResult> phone() {
        return uiThread(api.getBackend().phone());
    }

    public Single<List<PhoneResult>> searchPhonesNear(LatLng latLng, String query) {
        return uiThread(api.getBackend().searchPhonesNear($(LatLngStr.class).from(latLng), query));
    }

    public Single<SuccessResult> sendMessage(String phone, String message) {
        return uiThread(api.getBackend().sendMessage(phone, message));
    }

    public Single<Boolean> isVerified() {
        return uiThread(api.getBackend().getIsVerified())
                .map(verifiedResult -> verifiedResult.verified);
    }

    public Single<List<GroupMessageResult>> myMessages(LatLng latLng) {
        return uiThread(api.getBackend().myMessages($(LatLngStr.class).from(latLng)));
    }

    public Single<StateResult> myGroups(LatLng latLng) {
        return uiThread(api.getBackend().myGroups($(LatLngStr.class).from(latLng)));
    }

    public Single<GroupResult> getGroup(String groupId) {
        return uiThread(api.getBackend().getGroup(groupId));
    }

    public Single<SuccessResult> setPhoneNumber(String phoneNumber) {
        return uiThread(api.getBackend().setPhoneNumber(phoneNumber));
    }

    public Single<SuccessResult> sendVerificationCode(String verificationCode) {
        return uiThread(api.getBackend().sendVerificationCode(verificationCode));
    }

    public Single<CreateResult> sendGroupMessage(String groupId, String text, String attachment) {
        return uiThread(api.getBackend().sendGroupMessage(groupId, text, attachment));
    }

    public Single<SuccessResult> reactToMessage(String messageId, String reaction, boolean removeReaction) {
        return uiThread(api.getBackend().reactToMessage(messageId, reaction, removeReaction));
    }

    public Single<List<ReactionResult>> groupMessageReactions(String messageId) {
        return uiThread(api.getBackend().groupMessageReactions(messageId));
    }

    public Single<CreateResult> sendAreaMessage(LatLng latLng, String text, String attachment) {
        return uiThread(api.getBackend().sendAreaMessage($(LatLngStr.class).from(latLng), text, attachment));
    }

    public Single<CreateResult> createGroup(String groupName) {
        return uiThread(api.getBackend().createGroup(groupName));
    }

    public Single<CreateResult> createPublicGroup(String groupName, String about, LatLng latLng) {
        return uiThread(api.getBackend().createPublicGroup(groupName, about, $(LatLngStr.class).from(latLng), true));
    }

    public Single<CreateResult> createPhysicalGroup(LatLng latLng) {
        return uiThread(api.getBackend().createPhysicalGroup($(LatLngStr.class).from(latLng), true));
    }

    public Single<SuccessResult> convertToHub(String groupId, String name) {
        return uiThread(api.getBackend().convertToHub(groupId, name, true));
    }

    public Single<SuccessResult> inviteToGroup(String groupId, String name, String phoneNumber) {
        return uiThread(api.getBackend().inviteToGroup(groupId, name, phoneNumber));
    }

    public Single<SuccessResult> inviteToGroup(String groupId, String phoneId) {
        return uiThread(api.getBackend().inviteToGroup(groupId, phoneId));
    }

    public Single<SuccessResult> leaveGroup(String groupId) {
        return uiThread(api.getBackend().leaveGroup(groupId, true));
    }

    public Single<SuccessResult> setGroupPhoto(String groupId, String photo) {
        return uiThread(api.getBackend().setGroupPhoto(groupId, photo));
    }

    public Single<SuccessResult> setGroupAbout(String groupId, String about) {
        return uiThread(api.getBackend().setGroupAbout(groupId, about));
    }

    public Single<SuccessResult> removeGroupAction(String groupActionId) {
        return uiThread(api.getBackend().removeGroupAction(groupActionId));
    }

    public Single<CreateResult> createGroupAction(String groupId, String name, String intent) {
        return uiThread(api.getBackend().createGroupAction(groupId, name, intent));
    }

    public Single<List<GroupActionResult>> getGroupActions(String groupId) {
        return uiThread(api.getBackend().getGroupActions(groupId));
    }

    public Single<List<GroupActionResult>> getGroupActions(LatLng latLng) {
        return uiThread(api.getBackend().getGroupActionsNearGeo($(LatLngStr.class).from(latLng)));
    }

    public Single<List<GroupMessageResult>> getGroupMessages(String groupId) {
        return uiThread(api.getBackend().getGroupMessages(groupId));
    }

    public Single<GroupMessageResult> getGroupMessage(String groupMessageId) {
        return uiThread(api.getBackend().getGroupMessage(groupMessageId));
    }

    public Single<SuccessResult> setGroupActionPhoto(String groupActionId, String photo) {
        return uiThread(api.getBackend().setGroupActionPhoto(groupActionId, photo));
    }

    public Single<List<GroupMemberResult>> getAllGroupMember() {
        return uiThread(api.getBackend().getAllGroupMembers());
    }

    public Single<GroupMemberResult> getGroupMember(String groupId) {
        return uiThread(api.getBackend().getGroupMember(groupId));
    }

    public Single<CreateResult> updateGroupMember(String groupId, boolean muted, boolean subscribed) {
        return uiThread(api.getBackend().updateGroupMember(groupId, muted, subscribed));
    }

    public Single<SuccessResult> cancelInvite(String groupId, String groupInviteId) {
        return uiThread(api.getBackend().cancelInvite(groupId, groupInviteId));
    }

    public Single<CreateResult> createEvent(String name, String about, boolean isPublic, LatLng latLng, Date startsAt, Date endsAt) {
        return uiThread(api.getBackend().createEvent(name, about, isPublic, $(LatLngStr.class).from(latLng), $(HttpEncode.class).encode($(DateFormatter.class).format(startsAt)), $(HttpEncode.class).encode($(DateFormatter.class).format(endsAt))));
    }

    public Single<List<EventResult>> getEvents(LatLng latLng) {
        return uiThread(api.getBackend().getEvents($(LatLngStr.class).from(latLng)));
    }

    public Single<EventResult> getEvent(String eventId) {
        return uiThread(api.getBackend().getEvent(eventId));
    }

    public Single<SuccessResult> cancelEvent(String eventId) {
        return uiThread(api.getBackend().cancelEvent(eventId, true));
    }

    public Single<List<GroupResult>> getPhysicalGroups(LatLng latLng) {
        return uiThread(api.getBackend().getPhysicalGroups($(LatLngStr.class).from(latLng), "physical"));
    }

    public Single<String> uploadPhoto(InputStream photo) {
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

    public Single<String> privacy() {
        return uiThread(api.getContentBackend().privacy()).map(ResponseBody::string);
    }

    private <T> Single<T> uiThread(Single<T> observable) {
        return observable.observeOn(AndroidSchedulers.mainThread());
    }
}
