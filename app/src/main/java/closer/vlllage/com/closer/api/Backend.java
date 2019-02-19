package closer.vlllage.com.closer.api;

import java.util.List;

import closer.vlllage.com.closer.api.models.CreateResult;
import closer.vlllage.com.closer.api.models.EventResult;
import closer.vlllage.com.closer.api.models.GroupActionResult;
import closer.vlllage.com.closer.api.models.GroupMemberResult;
import closer.vlllage.com.closer.api.models.GroupMessageResult;
import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.PinResult;
import closer.vlllage.com.closer.api.models.ReactionResult;
import closer.vlllage.com.closer.api.models.StateResult;
import closer.vlllage.com.closer.api.models.SuccessResult;
import closer.vlllage.com.closer.api.models.SuggestionResult;
import closer.vlllage.com.closer.api.models.VerifiedResult;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Backend {

    String BASE_URL = "https://closer.vlllage.com/";

    // Phone

    @GET("map/{geo}")
    Single<List<PhoneResult>> getPhonesNear(@Path("geo") String geo);

    @POST("message/{message}")
    Single<SuccessResult> sendMessage(@Path("message") String phone, @Query("message") String message);

    @GET("message")
    Single<PhoneResult> phone();

    @POST("message")
    Single<CreateResult> phoneUpdate(
            @Query("geo") String latLng,
            @Query("name") String name,
            @Query("status") String status,
            @Query("active") Boolean active,
            @Query("deviceToken") String pushDeviceToken
    );

    @POST("message")
    Single<CreateResult> phoneUpdatePhoto(@Query("photo") String photo);

    @POST("message")
    Single<CreateResult> updatePhonePrivateMode(@Query("privateMode") boolean privateMode);

    @GET("message")
    Single<List<PhoneResult>> searchPhonesNear(@Query("geo") String latLng, @Query("query") String query);

    // Verify Number

    @GET("verify")
    Single<VerifiedResult> getIsVerified();

    @POST("verify")
    Single<SuccessResult> setPhoneNumber(@Query("set-number") String phoneNumber);

    @POST("verify")
    Single<SuccessResult> sendVerificationCode(@Query("verify-code") String verificationCode);

    // Suggestion

    @GET("suggestion/{latLng}")
    Single<List<SuggestionResult>> getSuggestionsNear(@Path("latLng") String latLng);

    @POST("suggestion")
    Single<CreateResult> addSuggestion(@Query("name") String name, @Query("geo") String geo);

    // Group Message

    @GET("message")
    Single<List<GroupMessageResult>> myMessages(@Query("geo") String latLng);

    @POST("message")
    Single<CreateResult> sendGroupMessage(@Query("group") String groupId, @Query("text") String text, @Query(value = "attachment", encoded = true) String attachment);

    @POST("message/{id}")
    Single<SuccessResult> reactToMessage(@Path("id") String messageId, @Query("react") String text, @Query("remove") boolean remove);

    @GET("message/{id}/reactions")
    Single<List<ReactionResult>> groupMessageReactions(@Path("id") String messageId);

    @POST("message")
    Single<CreateResult> sendAreaMessage(@Query("geo") String latLng, @Query("text") String text, @Query(value = "attachment", encoded = true) String attachment);

    @GET("message/{id}")
    Single<GroupMessageResult> getGroupMessage(@Path("id") String groupMessageId);

    @GET("group/{id}/messages")
    Single<List<GroupMessageResult>> getGroupMessages(@Path("id") String groupId);

    // Group

    @GET("group")
    Single<StateResult> myGroups(@Query("geo") String latLng);

    @POST("group")
    Single<CreateResult> createGroup(@Query("name") String groupName);

    @POST("group")
    Single<CreateResult> createPublicGroup(@Query("name") String groupName, @Query("about") String about, @Query("geo") String geo, @Query("public") boolean isPublic);

    @POST("group")
    Single<CreateResult> createPhysicalGroup(@Query("geo") String geo, @Query("physical") boolean physical);

    @GET("group")
    Single<List<GroupResult>> getPhysicalGroups(@Query("geo") String latLng, @Query("kind") String kind);

    @GET("group/{id}")
    Single<GroupResult> getGroup(@Path("id") String groupId);

    @POST("group/{id}")
    Single<SuccessResult> inviteToGroup(@Path("id") String groupId, @Query("name") String name, @Query("invite") String phoneNumber);

    @POST("group/{id}")
    Single<SuccessResult> inviteToGroup(@Path("id") String groupId, @Query("invite-message") String phoneId);

    @POST("group/{id}")
    Single<SuccessResult> leaveGroup(@Path("id") String groupId, @Query("leave") boolean leaveGroup);

    @POST("group/{id}")
    Single<SuccessResult> cancelInvite(@Path("id") String groupId, @Query("cancel-invite") String groupInviteId);

    @POST("group/{id}")
    Single<SuccessResult> convertToHub(@Path("id") String groupId, @Query("name") String name, @Query("hub") boolean hub);

    @POST("group/{id}")
    Single<SuccessResult> setGroupPhoto(@Path("id") String groupId, @Query("photo") String photo);

    @POST("group/{id}")
    Single<SuccessResult> setGroupAbout(@Path("id") String groupId, @Query("about") String about);

    @POST("group/{id}")
    Single<SuccessResult> pin(@Path("id") String groupId, @Query("pin") String messageId, @Query("remove") boolean remove);

    // Group Action

    @POST("action/{id}/delete")
    Single<SuccessResult> removeGroupAction(@Path("id") String groupActionId);

    @POST("action")
    Single<CreateResult> createGroupAction(@Query("group") String groupId, @Query("name") String groupActionName, @Query("intent") String groupActionIntent);

    @GET("group/{id}/actions")
    Single<List<GroupActionResult>> getGroupActions(@Path("id") String groupId);

    @GET("group/{id}/pins")
    Single<List<PinResult>> getPins(@Path("id") String groupId);

    @GET("action/{latLng}")
    Single<List<GroupActionResult>> getGroupActionsNearGeo(@Path("latLng") String latLng);

    @POST("action/{id}")
    Single<SuccessResult> setGroupActionPhoto(@Path("id") String groupActionId, @Query("photo") String photo);

    // Event

    @GET("event")
    Single<List<EventResult>> getEvents(@Query("geo") String latLng);

    @GET("event/{id}")
    Single<EventResult> getEvent(@Path("id") String eventId);

    @POST("event")
    Single<CreateResult> createEvent(@Query("name") String name, @Query("about") String about, @Query("public") boolean isPublic, @Query("geo") String geo, @Query(value = "starts-at", encoded = true) String startsAt, @Query(value = "ends-at", encoded = true) String endsAt);

    @POST("event/{id}")
    Single<SuccessResult> cancelEvent(@Path("id") String eventId, @Query("cancel") boolean cancel);

    // Member

    @GET("member")
    Single<List<GroupMemberResult>> getAllGroupMembers();

    @GET("member/of/{group}")
    Single<GroupMemberResult> getGroupMember(@Path("group") String groupId);

    @POST("member/of/{group}")
    Single<CreateResult> updateGroupMember(@Path("group") String groupId, @Query("muted") boolean muted, @Query("subscribed") boolean subscribed);
}
