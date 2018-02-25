package closer.vlllage.com.closer.util;

import java.util.Random;

public class PhoneUtil {
    public static String rndId() {
        Random random = new Random();
        return Long.toString(random.nextLong()) +
                Long.toString(random.nextLong()) +
                Long.toString(random.nextLong());
    }
}
