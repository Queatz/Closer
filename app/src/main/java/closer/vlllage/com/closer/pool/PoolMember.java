package closer.vlllage.com.closer.pool;

public class PoolMember {

    private Pool pool;

    PoolMember setPool(Pool pool) {
        this.pool = pool;
        onPoolInit();
        return this;
    }

    protected void onPoolInit() { }
    protected void onPoolEnd() { }

    protected <T extends PoolMember> T pool(Class<T> memeber) {
        return pool.with(memeber);
    }
}
