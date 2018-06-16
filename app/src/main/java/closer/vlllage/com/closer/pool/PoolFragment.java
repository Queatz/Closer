package closer.vlllage.com.closer.pool;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import closer.vlllage.com.closer.App;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;

public class PoolFragment extends Fragment {
    private final Pool pool = new Pool();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        $(ApplicationHandler.class).setApp((App) getActivity().getApplication());
        $(ActivityHandler.class).setActivity(getActivity());
    }

    @Override
    public void onDestroy() {
        pool.end();
        super.onDestroy();
    }

    protected <T extends PoolMember> T $(Class<T> member) {
        return pool.$(member);
    }
}
