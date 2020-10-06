package closer.vlllage.com.closer.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET

interface ContentBackend {

    @GET("privacy")
    fun privacy(): Single<ResponseBody>

    @GET("terms")
    fun terms(): Single<ResponseBody>

    companion object {
        const val BASE_URL = "https://closer.vlllage.com/"
    }
}
