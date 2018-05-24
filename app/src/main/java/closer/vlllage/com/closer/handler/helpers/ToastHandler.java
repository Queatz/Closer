package closer.vlllage.com.closer.handler.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;

import closer.vlllage.com.closer.pool.PoolMember;

public class ToastHandler extends PoolMember {

    public void show(@StringRes int message) {
        Toast.makeText($(ApplicationHandler.class).getApp(), message, Toast.LENGTH_SHORT).show();
    }

    public void show(@NonNull String message) {
        Toast.makeText($(ApplicationHandler.class).getApp(), message, Toast.LENGTH_SHORT).show();
    }
}
