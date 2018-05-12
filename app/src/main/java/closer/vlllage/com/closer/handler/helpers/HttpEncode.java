package closer.vlllage.com.closer.handler.helpers;

import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import closer.vlllage.com.closer.pool.PoolMember;

public class HttpEncode extends PoolMember {

    private static final String UTF_8 = "UTF-8";

    @Nullable
    public String encode(@Nullable String string) {
        if (string == null) {
            return null;
        }

        try {
            return URLEncoder.encode(string, UTF_8);
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
            return URLDecoder.decode(string, UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
