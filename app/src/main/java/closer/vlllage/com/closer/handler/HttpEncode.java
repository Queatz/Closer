package closer.vlllage.com.closer.handler;

import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import closer.vlllage.com.closer.pool.PoolMember;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpEncode extends PoolMember {

    @Nullable
    public String encode(@Nullable String string) {
        if (string == null) {
            return null;
        }

        try {
            return URLEncoder.encode(string, UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public String decode(@Nullable String string) {
        if (string == null) {
            return null;
        }

        try {
            return URLDecoder.decode(string, UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
