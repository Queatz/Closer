package closer.vlllage.com.closer.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;

public interface ContentBackend {
    String BASE_URL = "http://closer.vlllage.com/";

    @GET("privacy")
    Observable<ResponseBody> privacy();
}
