package closer.vlllage.com.closer.handler;

import closer.vlllage.com.closer.pool.PoolMember;

public class Val extends PoolMember {
    public String of(String string) {
        return string == null ? "" : string.trim();
    }
}
