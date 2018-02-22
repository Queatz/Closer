package closer.vlllage.com.closer.api;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.SuccessResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Backend {
    String BASE_URL = "http://localhost:8080/closer/";

    @GET("/near/:latLng")
    Call<List<PhoneResult>> near(@Path("latLng") String latLng);

    @POST("/send/:phone")
    Call<SuccessResult> send(@Path("phone") String phone, @Query("message") String message);

    @GET("/phone")
    Call<PhoneResult> phone();

    @POST("/phone")
    Call<SuccessResult> phoneUpdate(
            @Query("location") LatLng latLng,
            @Query("name") String name,
            @Query("status") String status,
            @Query("active") boolean active,
            @Query("pushDeviceToken") String pushDeviceToken
    );

}
