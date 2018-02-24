package closer.vlllage.com.closer.pool;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import closer.vlllage.com.closer.handler.ActivityHandler;
import closer.vlllage.com.closer.handler.PermissionHandler;

public class PoolActivity extends FragmentActivity {
    private final Pool pool = new Pool();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pool.$(ActivityHandler.class).setActivity(this);
    }

    @Override
    protected void onDestroy() {
        pool.end();
        super.onDestroy();
    }

    protected <T extends PoolMember> T $(Class<T> member) {
        return pool.$(member);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        $(PermissionHandler.class).onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
