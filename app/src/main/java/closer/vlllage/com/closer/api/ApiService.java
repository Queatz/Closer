package closer.vlllage.com.closer.api;

import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.util.logging.Logger;

import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    private static final String AUTHORIZATION = "Authorization";
    private final Backend backend;
    private String authorization;

    public ApiService() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader(AUTHORIZATION, authorization).build();
            return chain.proceed(request);
        });

        httpClient.addInterceptor(chain -> {
            Logger.getAnonymousLogger().warning("NETWORK: REQUEST: " + chain.request().toString());

            Response response = chain.proceed(chain.request());

            if (response.body() == null) {
                Logger.getAnonymousLogger().warning("NETWORK: RESPONSE: null");
            } else if (response.body().contentLength() < 0) {
                Logger.getAnonymousLogger().warning("NETWORK: RESPONSE: no content");
            } else {
                Logger.getAnonymousLogger().warning("NETWORK: RESPONSE: " + response.peekBody(response.body().contentLength()).string());
            }

            return response;
        });

        backend = new Retrofit.Builder()
                .baseUrl(Backend.BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat(DateFormat.FULL).create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(Backend.class);
    }

    public Backend getBackend() {
        if (authorization == null) {
            throw new RuntimeException("Must set 'Authorization' header prior to using backend");
        }

        return backend;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
}
