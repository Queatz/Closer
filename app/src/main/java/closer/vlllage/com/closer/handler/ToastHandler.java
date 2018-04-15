package closer.vlllage.com.closer.handler;

import android.support.annotation.StringRes;
import android.widget.Toast;

import closer.vlllage.com.closer.pool.PoolMember;

public class ToastHandler extends PoolMember {

    public void show(@StringRes int message) {
        Toast.makeText($(ApplicationHandler.class).getApp(), message, Toast.LENGTH_SHORT).show();

    }
}
