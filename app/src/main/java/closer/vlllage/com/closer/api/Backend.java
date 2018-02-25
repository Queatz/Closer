package closer.vlllage.com.closer.api;

import java.util.List;

import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.SuccessResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Backend {
    String BASE_URL = "http://closer.vlllage.com/";

    @GET("map/{latLng}")
    Observable<List<PhoneResult>> near(@Path("latLng") String latLng);

    @POST("phone/{phone}")
    Observable<SuccessResult> send(@Path("phone") String phone, @Query("message") String message);

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

}
