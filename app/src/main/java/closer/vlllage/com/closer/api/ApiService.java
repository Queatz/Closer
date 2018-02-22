package closer.vlllage.com.closer.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    private final Backend backend;

    public ApiService() {
        backend = new Retrofit.Builder()
                .baseUrl(Backend.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Backend.class);
    }

    public Backend getBackend() {
        return backend;
    }
}
