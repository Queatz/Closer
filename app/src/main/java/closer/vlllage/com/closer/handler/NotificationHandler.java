package closer.vlllage.com.closer.handler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

import closer.vlllage.com.closer.Background;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.MapsActivity;
import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

import static android.content.Context.NOTIFICATION_SERVICE;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_LAT_LNG;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_NAME;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_STATUS;

public class NotificationHandler extends PoolMember {

    public static final String KEY_TEXT_REPLY = "key_text_reply";
    public static final int NOTIFICATION_ID = 0;
    private static final int REQUEST_CODE_NOTIFICATION = 101;
    private static final String NOTIFICATION_CHANNEL = "notifications";

    public void showNotification(MapBubble mapBubble) {
        Context context = $(ActivityHandler.class).getActivity().getBaseContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    context.getString(R.string.closer_notifications),
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);
        }

        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(context.getString(R.string.reply))
                .build();

        Intent intent = new Intent(context, MapsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_LAT_LNG, new float[] {
                (float) mapBubble.getLatLng().latitude,
                (float) mapBubble.getLatLng().longitude
        });
        intent.putExtra(EXTRA_NAME, mapBubble.getName());
        intent.putExtra(EXTRA_STATUS, mapBubble.getStatus());

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        PendingIntent replyPendingIntent =
                PendingIntent.getBroadcast(context,
                        REQUEST_CODE_NOTIFICATION,
                        new Intent(context, Background.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_launcher_foreground,
                        context.getString(R.string.reply), replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        Notification newMessageNotification =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(mapBubble.getName())
                        .setContentText(mapBubble.getStatus())
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent)
                        .addAction(action)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, newMessageNotification);
    }
}
