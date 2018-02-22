package closer.vlllage.com.closer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.widget.Toast;

import static closer.vlllage.com.closer.MapsActivity.KEY_TEXT_REPLY;
import static closer.vlllage.com.closer.MapsActivity.NOTIFICATION_ID;

public class Background extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null) {
                Toast.makeText(context, remoteInput.getCharSequence(KEY_TEXT_REPLY), Toast.LENGTH_SHORT).show();

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(NOTIFICATION_ID);
            }
        }
    }
}
