package closer.vlllage.com.closer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.widget.Toast;

import closer.vlllage.com.closer.handler.AccountHandler;
import closer.vlllage.com.closer.handler.ApiHandler;
import closer.vlllage.com.closer.handler.DisposableHandler;

import static closer.vlllage.com.closer.MapsActivity.EXTRA_PHONE;
import static closer.vlllage.com.closer.handler.NotificationHandler.EXTRA_NOTIFICATION;
import static closer.vlllage.com.closer.handler.NotificationHandler.KEY_TEXT_REPLY;
import static closer.vlllage.com.closer.handler.NotificationHandler.NOTIFICATION_ID;

public class Background extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null) {
                App app = (App) context.getApplicationContext();

                String phone = intent.getStringExtra(EXTRA_PHONE);
                CharSequence replyMessage = remoteInput.getCharSequence(KEY_TEXT_REPLY);

                if(replyMessage == null || replyMessage.length() < 1) {
                    return;
                }

                if (phone == null) {
                    return;
                }

                app.$(ApiHandler.class).setAuthorization(app.$(AccountHandler.class).getPhone());

                app.$(DisposableHandler.class).add(app.$(ApiHandler.class).sendMessage(phone, replyMessage.toString()).subscribe(successResult -> {
                    if (!successResult.success) {
                        Toast.makeText(app, R.string.message_not_sent, Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    Toast.makeText(app, R.string.message_not_sent, Toast.LENGTH_SHORT).show();
                }));

                if (intent.hasExtra(EXTRA_NOTIFICATION)) {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.cancel(intent.getStringExtra(EXTRA_NOTIFICATION), NOTIFICATION_ID);
                }
            }
        }
    }
}
