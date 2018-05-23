package closer.vlllage.com.closer.handler.data;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.Background;
import closer.vlllage.com.closer.GroupActivity;
import closer.vlllage.com.closer.MapsActivity;
import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.event.EventDetailsHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Event;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static closer.vlllage.com.closer.GroupActivity.EXTRA_GROUP_ID;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_LAT_LNG;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_NAME;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_PHONE;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_STATUS;

public class NotificationHandler extends PoolMember {

    public static final String KEY_TEXT_REPLY = "key_text_reply";
    public static final int NOTIFICATION_ID = 0;
    private static final int REQUEST_CODE_NOTIFICATION = 101;
    public static final String EXTRA_NOTIFICATION = "notification";
    public static final String EXTRA_MUTE = "mute";

    public void showBubbleMessageNotification(String phone, LatLng latLng, String name, String message) {
        Context context = $(ApplicationHandler.class).getApp();

        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(context.getString(R.string.reply))
                .build();

        Intent intent = new Intent(context, MapsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);

        if (latLng != null) {
            intent.putExtra(EXTRA_LAT_LNG, new float[]{
                    (float) latLng.latitude,
                    (float) latLng.longitude
            });
        }

        name = name.isEmpty() ?
                $(ResourcesHandler.class).getResources().getString(R.string.app_name) :
                name;

        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_STATUS, message);
        intent.putExtra(EXTRA_PHONE, phone);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        String notificationTag = phone + "/" + $(Val.class).rndId();

        Intent backgroundIntent = new Intent(context, Background.class);
        backgroundIntent.putExtra(EXTRA_PHONE, phone);
        backgroundIntent.putExtra(EXTRA_NOTIFICATION, notificationTag);

        show(contentIntent, backgroundIntent, remoteInput, name, message, notificationTag, true);
    }

    public void showInvitedToGroupNotification(String invitedBy, String groupName, String groupId) {
        Context context = $(ApplicationHandler.class).getApp();

        Intent intent = new Intent(context, GroupActivity.class);
        intent.setAction(Intent.ACTION_VIEW);

        intent.putExtra(EXTRA_GROUP_ID, groupId);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        show(contentIntent, null, null, $(ResourcesHandler.class).getResources().getString(R.string.app_name),
                $(ResourcesHandler.class).getResources().getString(R.string.invited_to_group_notification, invitedBy, groupName),
                groupId + "/invited", true);
    }

    public void showGroupMessageNotification(String text, String messageFrom, String groupName, String groupId, String isPassive) {
        Context context = $(ApplicationHandler.class).getApp();

        Intent intent = new Intent(context, GroupActivity.class);
        intent.setAction(Intent.ACTION_VIEW);

        intent.putExtra(EXTRA_GROUP_ID, groupId);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        show(contentIntent, null, null,
                $(ResourcesHandler.class).getResources().getString(R.string.group_message_notification, messageFrom, groupName),
                text,
                groupId + "/message", !Boolean.valueOf(isPassive));
    }

    public void showEventNotification(Event event) {
        Context context = $(ApplicationHandler.class).getApp();

        Intent intent = new Intent(context, GroupActivity.class);
        intent.setAction(Intent.ACTION_VIEW);

        intent.putExtra(EXTRA_GROUP_ID, event.getGroupId());

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        show(contentIntent, null, null,
                $(ResourcesHandler.class).getResources().getString(R.string.event_notification, event.getName(), $(EventDetailsHandler.class).formatRelative(event.getStartsAt())),
                $(EventDetailsHandler.class).formatEventDetails(event),
                event.getId() + "/group", false);
    }

    public void hide(String notificationTag) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from($(ApplicationHandler.class).getApp());
        notificationManager.cancel(notificationTag, NOTIFICATION_ID);
    }

    private void show(PendingIntent contentIntent, Intent backgroundIntent,
                      RemoteInput remoteInput,
                      String name,
                      String message,
                      String notificationTag,
                      boolean sound) {
        Context context = $(ApplicationHandler.class).getApp();

        if ($(PersistenceHandler.class).getIsNotifcationsPaused()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationChannel(),
                    context.getString(R.string.closer_notifications),
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, notificationChannel())
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(name)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

        if (sound) {
            builder.setDefaults(Notification.DEFAULT_ALL);
        }

        if (remoteInput != null) {
            PendingIntent replyPendingIntent =
                    PendingIntent.getBroadcast(context,
                            REQUEST_CODE_NOTIFICATION,
                            backgroundIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.ic_notification,
                            $(ResourcesHandler.class).getResources().getString(R.string.reply), replyPendingIntent)
                            .addRemoteInput(remoteInput)
                            .build();

            builder.addAction(action);
        }

        Intent muteBackgroundIntent = new Intent(context, Background.class);
        muteBackgroundIntent.putExtra(EXTRA_MUTE, true);
        muteBackgroundIntent.putExtra(EXTRA_NOTIFICATION, notificationTag);

        PendingIntent mutePendingIntent =
                PendingIntent.getBroadcast(context,
                        REQUEST_CODE_NOTIFICATION,
                        muteBackgroundIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action muteAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_notifications_paused_white_24dp,
                        $(ResourcesHandler.class).getResources().getString(R.string.mute), mutePendingIntent)
                        .build();

        builder.addAction(muteAction);

        Notification newMessageNotification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationTag, NOTIFICATION_ID, newMessageNotification);

        ensureScreenIsOn(context);
    }

    private String notificationChannel() {
        return $(ResourcesHandler.class).getResources().getString(R.string.notification_channel);
    }

    private void ensureScreenIsOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if(pm != null && !pm.isInteractive()) {
            PowerManager.WakeLock wl = pm.newWakeLock(FLAG_KEEP_SCREEN_ON | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,"closer:notification");
            wl.acquire(5000);
        }
    }
}