package closer.vlllage.com.closer.api;

import java.util.List;

import closer.vlllage.com.closer.api.models.CreateResult;
import closer.vlllage.com.closer.api.models.GroupMessageResult;
import closer.vlllage.com.closer.api.models.GroupResult;
import closer.vlllage.com.closer.api.models.PhoneResult;
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

    // Phone

    @GET("map/{geo}")
    Observable<List<PhoneResult>> getPhonesNear(@Path("geo") String geo);

    @POST("phone/{phone}")
    Observable<SuccessResult> sendMessage(@Path("phone") String phone, @Query("message") String message);

    @GET("phone")
    Observable<PhoneResult> phone();

    @POST("phone")
    Observable<SuccessResult> phoneUpdate(
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
    Observable<List<GroupMessageResult>> myMessages();

    @POST("message")
    Observable<CreateResult> sendGroupMessage(@Query("group") String groupId, @Query("text") String text, @Query("attachment") String attachment);

    // Group

    @GET("group")
    Observable<List<GroupResult>> myGroups();

    @POST("group")
    Observable<CreateResult> createGroup(@Query("name") String groupName);

    @POST("group/{id}")
    Observable<SuccessResult> inviteToGroup(@Path("id") String groupId, @Query("invite") String phoneNumber);

    @POST("group/{id}")
    Observable<SuccessResult> leaveGroup(@Path("id") String groupId, @Query("leave") boolean leaveGroup);
}
