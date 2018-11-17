package closer.vlllage.com.closer.pool;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Pool {

    private final Map<Class, PoolMember> members = new HashMap<>();

    Pool() {}

    public static TempPool tempPool() {
        return new TempPool();
    }

    @SuppressWarnings("unchecked")
    public <T extends PoolMember> T $(Class<T> member) {
        if (!members.containsKey(member)) {
            try {
                members.put(member, member.getConstructor().newInstance().setPool(this));
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return (T) members.get(member);
    }

    public <T extends PoolMember> T $set(T member) {
        if (members.containsKey(member.getClass())) {
            throw new IllegalStateException("Cannot $set member that already exists");
        }

        members.put(member.getClass(), member);

        return (T) members.get(member);
    }

    protected void end() {
        for (PoolMember member : members.values()) {
            member.onPoolEnd();
        }
    }
}
