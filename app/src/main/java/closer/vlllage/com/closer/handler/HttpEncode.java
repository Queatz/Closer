package closer.vlllage.com.closer.handler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import closer.vlllage.com.closer.pool.PoolMember;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpEncode extends PoolMember {

    public String encode(String string) {
        try {
            return URLEncoder.encode(string, UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decode(String string) {
        try {
            return URLDecoder.decode(string, UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
