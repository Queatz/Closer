package closer.vlllage.com.closer.api

import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.util.logging.Logger

class ApiService {
    val backend: Backend
    val photoUploadBackend: PhotoUploadBackend
    val contentBackend: ContentBackend
    lateinit var authorization: String

    init {
        val httpClient = OkHttpClient.Builder()

        httpClient.addInterceptor { chain ->
            val request = chain.request().newBuilder()
                    .addHeader(AUTHORIZATION, authorization)
                    .addHeader(X_CLOSER_APP_ID, X_CLOSER_APP_ID_VALUE)
                    .build()
            chain.proceed(request)
        }

        httpClient.addInterceptor { chain ->
            Logger.getAnonymousLogger().warning("NETWORK: REQUEST: " + chain.request().toString())

            val response = chain.proceed(chain.request())

            if (response.body() == null) {
                Logger.getAnonymousLogger().warning("NETWORK: RESPONSE: null")
            } else if (response.body()!!.contentLength() < 0) {
                Logger.getAnonymousLogger().warning("NETWORK: RESPONSE: no content")
            } else {
                Logger.getAnonymousLogger().warning("NETWORK: RESPONSE: " + response.peekBody(response.body()!!.contentLength()).string())
            }

            response
        }

        backend = Retrofit.Builder()
                .baseUrl(Backend.BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setDateFormat(DateFormat.FULL).create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(Backend::class.java)

        val httpPhotoUploadClient = OkHttpClient.Builder()

        httpPhotoUploadClient.addInterceptor { chain ->
            val request = chain.request().newBuilder().addHeader("X-CLOSER-UPLOAD", "iamsupersupersecret").build()
            chain.proceed(request)
        }

        photoUploadBackend = Retrofit.Builder()
                .baseUrl(PhotoUploadBackend.BASE_URL)
                .client(httpPhotoUploadClient.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(PhotoUploadBackend::class.java)

        contentBackend = Retrofit.Builder()
                .baseUrl(ContentBackend.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(ContentBackend::class.java)
    }

    companion object {
        private const val AUTHORIZATION = "Authorization"
        private const val X_CLOSER_APP_ID = "X-CLOSER-APP-ID"
        private const val X_CLOSER_APP_ID_VALUE = "closer-app-v1-5109"
    }
}
