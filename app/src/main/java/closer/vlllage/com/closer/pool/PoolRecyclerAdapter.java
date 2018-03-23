package closer.vlllage.com.closer.pool;

import android.support.v7.widget.RecyclerView;

public abstract class PoolRecyclerAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    private Pool pool;

    public PoolRecyclerAdapter(PoolMember poolMember) {
        this.pool = poolMember.pool;
    }

    protected <T extends PoolMember> T $(Class<T> member) {
        return pool.$(member);
    }
}
