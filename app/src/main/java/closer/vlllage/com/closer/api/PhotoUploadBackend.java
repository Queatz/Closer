package closer.vlllage.com.closer.api;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface PhotoUploadBackend {
    String BASE_URL = "http://closer-files.vlllage.com/";

    @Multipart
    @POST("{id}")
    Observable<ResponseBody> uploadPhoto(@Path("id") String photoId,
                                         @Part MultipartBody.Part photo);
}
