package closer.vlllage.com.closer.pool;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import closer.vlllage.com.closer.App;
import closer.vlllage.com.closer.handler.data.PermissionHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.CameraHandler;
import closer.vlllage.com.closer.handler.media.MediaHandler;

public abstract class PoolActivity extends FragmentActivity {
    private final Pool pool = new Pool();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        $(ApplicationHandler.class).setApp((App) getApplication());
        $(ActivityHandler.class).setActivity(this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        $(CameraHandler.class).onActivityResult(requestCode, resultCode, data);
        $(MediaHandler.class).onActivityResult(requestCode, resultCode, data);
    }
}
