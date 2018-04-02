package closer.vlllage.com.closer.handler;

import com.google.gson.Gson;

import closer.vlllage.com.closer.pool.PoolMember;

public class JsonHandler extends PoolMember {

    private Gson gson = new Gson();

    public <T> T from(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public String to(Object obj) {
        return gson.toJson(obj);
    }
}
