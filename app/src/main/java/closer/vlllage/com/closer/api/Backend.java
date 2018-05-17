package closer.vlllage.com.closer.api;

import java.util.List;

import closer.vlllage.com.closer.api.models.CreateResult;
import closer.vlllage.com.closer.api.models.EventResult;
import closer.vlllage.com.closer.api.models.GroupMessageResult;
import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.StateResult;
import closer.vlllage.com.closer.api.models.SuccessResult;
import closer.vlllage.com.closer.api.models.SuggestionResult;
import closer.vlllage.com.closer.api.models.VerifiedResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Backend {
    String BASE_URL = "http://closer.vlllage.com/";
//    String BASE_URL = "http://10.0.2.2:8080/closer/";

    // Phone

    @GET("map/{geo}")
    Observable<List<PhoneResult>> getPhonesNear(@Path("geo") String geo);

    @POST("phone/{phone}")
    Observable<SuccessResult> sendMessage(@Path("phone") String phone, @Query("message") String message);

    @GET("phone")
    Observable<PhoneResult> phone();

    @POST("phone")
    Observable<CreateResult> phoneUpdate(
            @Query("geo") String latLng,
            @Query("name") String name,
            @Query("status") String status,
            @Query("active") Boolean active,
            @Query("deviceToken") String pushDeviceToken
    );

    // Verify Number

    @GET("verify")
    Observable<VerifiedResult> getIsVerified();

    @POST("verify")
    Observable<SuccessResult> setPhoneNumber(@Query("set-number") String phoneNumber);

    @POST("verify")
    Observable<SuccessResult> sendVerificationCode(@Query("verify-code") String verificationCode);

    // Suggestion

    @GET("suggestion/{latLng}")
    Observable<List<SuggestionResult>> getSuggestionsNear(@Path("latLng") String latLng);

    @POST("suggestion")
    Observable<CreateResult> addSuggestion(@Query("name") String name, @Query("geo") String geo);

    // Group Message

    @GET("message")
    Observable<List<GroupMessageResult>> myMessages(@Query("geo") String latLng);

    @POST("message")
    Observable<CreateResult> sendGroupMessage(@Query("group") String groupId, @Query("text") String text, @Query(value = "attachment", encoded = true) String attachment);

    // Group

    @GET("group")
    Observable<StateResult> myGroups(@Query("geo") String latLng);

    @POST("group")
    Observable<CreateResult> createGroup(@Query("name") String groupName);

    @POST("group")
    Observable<CreateResult> createPublicGroup(@Query("name") String groupName, @Query("about") String about, @Query("geo") String geo, @Query("public") boolean isPublic);

    @POST("group")
    Observable<CreateResult> createPhysicalGroup(@Query("geo") String geo, @Query("physical") boolean physical);

    @GET("group/{id}")
    Observable<GroupResult> getGroup(@Path("id") String groupId);

    @POST("group/{id}")
    Observable<SuccessResult> inviteToGroup(@Path("id") String groupId, @Query("name") String name, @Query("invite") String phoneNumber);

    @POST("group/{id}")
    Observable<SuccessResult> leaveGroup(@Path("id") String groupId, @Query("leave") boolean leaveGroup);

    @POST("group/{id}")
    Observable<SuccessResult> cancelInvite(@Path("id") String groupId, @Query("cancel-invite") String groupInviteId);

    @POST("group/{id}")
    Observable<SuccessResult> convertToHub(@Path("id") String groupId, @Query("hub") boolean hub);

    // Event

    @GET("event")
    Observable<List<EventResult>> getEvents(@Query("geo") String latLng);

    @GET("event/{id}")
    Observable<EventResult> getEvent(@Path("id") String eventId);

    @POST("event")
    Observable<CreateResult> createEvent(@Query("name") String name, @Query("about") String about, @Query("geo") String geo, @Query(value = "starts-at", encoded = true) String startsAt, @Query(value = "ends-at", encoded = true) String endsAt);

    @POST("event/{id}")
    Observable<SuccessResult> cancelEvent(@Path("id") String eventId, @Query("cancel") boolean cancel);
}
