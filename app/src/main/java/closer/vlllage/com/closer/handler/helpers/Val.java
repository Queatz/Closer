package closer.vlllage.com.closer.handler.helpers;

import java.util.Random;

import closer.vlllage.com.closer.pool.PoolMember;

import static com.google.android.gms.common.util.Strings.isEmptyOrWhitespace;

public class Val extends PoolMember {
    public String rndId() {
        Random random = new Random();
        return Long.toString(random.nextLong()) +
                Long.toString(random.nextLong()) +
                Long.toString(random.nextLong());
    }

    public String of(String string) {
        return string == null ? "" : string.trim();
    }

    public String of(String string, String stringWhenEmpty) {
        return isEmpty(string) ? stringWhenEmpty : string;
    }

    public boolean isEmpty(String string) {
        return isEmptyOrWhitespace(string);
    }
}
