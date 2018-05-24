package closer.vlllage.com.closer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;

import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.NotificationHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ToastHandler;

import static closer.vlllage.com.closer.MapsActivity.EXTRA_PHONE;
import static closer.vlllage.com.closer.handler.data.NotificationHandler.EXTRA_MUTE;
import static closer.vlllage.com.closer.handler.data.NotificationHandler.EXTRA_NOTIFICATION;
import static closer.vlllage.com.closer.handler.data.NotificationHandler.KEY_TEXT_REPLY;

public class Background extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        App app = (App) context.getApplicationContext();

        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_MUTE, false)) {
                app.$(PersistenceHandler.class).setIsNotificationsPaused(true);
            } else {
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                if (remoteInput != null) {
                    String phone = intent.getStringExtra(EXTRA_PHONE);
                    CharSequence replyMessage = remoteInput.getCharSequence(KEY_TEXT_REPLY);

                    if (replyMessage == null || replyMessage.length() < 1) {
                        return;
                    }

                    if (phone == null) {
                        return;
                    }

                    app.$(ApiHandler.class).setAuthorization(app.$(AccountHandler.class).getPhone());

                    app.$(DisposableHandler.class).add(app.$(ApiHandler.class).sendMessage(phone, replyMessage.toString()).subscribe(successResult -> {
                        if (successResult.success) {
                            app.$(ToastHandler.class).show(R.string.message_sent);
                        } else {
                            app.$(ToastHandler.class).show(R.string.message_not_sent);
                        }
                    }, error -> {
                        app.$(ToastHandler.class).show(R.string.message_not_sent);
                    }));
                }
            }

            if (intent.hasExtra(EXTRA_NOTIFICATION)) {
                app.$(NotificationHandler.class).hide(intent.getStringExtra(EXTRA_NOTIFICATION));
            }
        }
    }
}
