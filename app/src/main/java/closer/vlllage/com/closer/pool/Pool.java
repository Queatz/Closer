package closer.vlllage.com.closer.pool;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Pool {

    private final Map<Class, PoolMember> members = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends PoolMember> T with(Class<T> member) {
        if (!members.containsKey(member)) {
            try {
                members.put(member, member.getConstructor().newInstance().setPool(this));
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return (T) members.get(member);
    }

    public void end() {
        for (PoolMember member : members.values()) {
            member.onPoolEnd();
        }
    }
}
