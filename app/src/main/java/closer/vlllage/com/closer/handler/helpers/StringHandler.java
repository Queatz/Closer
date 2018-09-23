package closer.vlllage.com.closer.handler.helpers;

import closer.vlllage.com.closer.pool.PoolMember;

public class StringHandler extends PoolMember {
    public boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
