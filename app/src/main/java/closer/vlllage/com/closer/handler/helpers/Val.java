package closer.vlllage.com.closer.handler.helpers;

import java.util.Random;

import closer.vlllage.com.closer.pool.PoolMember;

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
}
