package closer.vlllage.com.closer.handler.helpers;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import closer.vlllage.com.closer.pool.PoolMember;
import okhttp3.OkHttpClient;

public class ImageHandler extends PoolMember {

    private static Picasso picasso;

    @Override
    protected void onPoolInit() {
        if (picasso == null) picasso = new Picasso.Builder($(ApplicationHandler.class).getApp())
                .downloader(new OkHttp3Downloader(new OkHttpClient()))
                .build();
    }

    public Picasso get() {
        return picasso;
    }
}
