package closer.vlllage.com.closer.handler;

import android.app.Activity;

import closer.vlllage.com.closer.pool.PoolMember;

public class ActivityHandler extends PoolMember {

    private Activity activity;

    public Activity getActivity() {
        if (this.activity == null) {
            throw new IllegalStateException("Activity was not set!");
        }

        return activity;
    }

    public void setActivity(Activity activity) {
        if (this.activity != null) {
            throw new IllegalStateException("Cannot set Activity twice! Use another pool.");
        }

        this.activity = activity;
    }
}
