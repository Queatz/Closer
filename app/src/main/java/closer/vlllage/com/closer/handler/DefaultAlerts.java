package closer.vlllage.com.closer.handler;

import android.support.annotation.StringRes;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class DefaultAlerts extends PoolMember {

    public void thatDidntWork() {
        thatDidntWork(null);
    }

    public void thatDidntWork(String message) {
        $(AlertHandler.class).make()
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.that_didnt_work))
                .setMessage(message)
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                .show();
    }

    public void syncError() {
        $(AlertHandler.class).make()
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.sync_didnt_work))
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.boo))
                .show();
    }

    public void longMessage(@StringRes Integer title, @StringRes int message) {
        $(AlertHandler.class).make()
                .setTitle(title == null ? null : $(ResourcesHandler.class).getResources().getString(title))
                .setLayoutResId(R.layout.long_message_modal)
                .setOnAfterViewCreated(view -> {
                    ((TextView) view.findViewById(R.id.messageText)).setText(message);
                })
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                .show();
    }
}
