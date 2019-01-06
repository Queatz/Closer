package closer.vlllage.com.closer.handler.helpers;

import java.util.List;

import closer.vlllage.com.closer.pool.PoolMember;

public class ListEqual extends PoolMember {
    public boolean isEqual(List a, List b) {
        if (a == null || b == null) {
            return (a == null) && (b == null);
        }

        if ( a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i++) {
            Object oA = a.get(i);
            Object oB = a.get(i);
            if ((oA == null && oB != null) || (oA != null && !oA.equals(oB))) {
                return false;
            }
        }

        return true;
    }
}
