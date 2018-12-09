package closer.vlllage.com.closer.handler.helpers;

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
        longMessage(title, message);
    }

    public void longMessage(@StringRes Integer title, CharSequence message) {
        $(AlertHandler.class).make()
                .setTitle(title == null ? null : $(ResourcesHandler.class).getResources().getString(title))
                .setLayoutResId(R.layout.long_message_modal)
                .setOnAfterViewCreated((alertConfig, view) -> {
                    ((TextView) view.findViewById(R.id.messageText)).setText(message);
                })
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                .show();
    }

    public void message(String message) {
        $(AlertHandler.class).make()
                .setMessage(message)
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                .show();
    }

    public void message(String message, AlertConfig.ButtonCallback buttonCallback) {
        $(AlertHandler.class).make()
                .setMessage(message)
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                .setPositiveButtonCallback(buttonCallback)
                .show();
    }

    public void message(String title, String message) {
        $(AlertHandler.class).make()
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                .show();
    }

    public void message(@StringRes int titleRes, @StringRes int messageRes) {
        message($(ResourcesHandler.class).getResources().getString(titleRes),
                $(ResourcesHandler.class).getResources().getString(messageRes));
    }

    public void message(@StringRes int stringRes) {
        message($(ResourcesHandler.class).getResources().getString(stringRes));
    }
}
