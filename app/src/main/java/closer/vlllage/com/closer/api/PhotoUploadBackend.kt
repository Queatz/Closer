package closer.vlllage.com.closer.api

import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PhotoUploadBackend {

    @Multipart
    @POST("{id}")
    fun uploadPhoto(@Path("id") photoId: String,
                    @Part photo: MultipartBody.Part): Single<ResponseBody>

    companion object {
        const val BASE_URL = "http://closer-files.vlllage.com/"
    }
}
