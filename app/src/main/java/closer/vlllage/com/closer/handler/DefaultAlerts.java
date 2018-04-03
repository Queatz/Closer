package closer.vlllage.com.closer.handler;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class DefaultAlerts extends PoolMember {

    public void thatDidntWork() {
        $(AlertHandler.class).make()
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.that_didnt_work))
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                .show();
    }

    public void syncError() {
        $(AlertHandler.class).make()
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.sync_didnt_work))
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.boo))
                .show();
    }
}
