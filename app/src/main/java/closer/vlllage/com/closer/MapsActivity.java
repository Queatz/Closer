package closer.vlllage.com.closer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_PERMISSION = 100;
    private static final int REQUEST_CODE_NOTIFICATION = 101;
    private static final String NOTIFICATION_CHANNEL = "notifications";
    public static final String KEY_TEXT_REPLY = "key_text_reply";
    public static final int NOTIFICATION_ID = 0;

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProvider;
    private View replyLayout;
    private View myStatusLayout;

    private BubbleMapLayer bubbleMapLayer = new BubbleMapLayer();
    private MapBubble replyingToMapBubble;
    private View sendButton;
    private EditText replyMessage;
    private LatLng centerOnMapLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        replyLayout = findViewById(R.id.replyLayout);
        sendButton = replyLayout.findViewById(R.id.sendButton);

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this);

        replyMessage = replyLayout.findViewById(R.id.message);

        sendButton.setOnClickListener(view -> {
            showNotification(replyingToMapBubble);
            showReplyLayout(false);
        });

        replyMessage.setOnEditorActionListener((textView, action, keyEvent) -> {
            if (action == EditorInfo.IME_ACTION_GO) {
                showNotification(replyingToMapBubble);
                showReplyLayout(false);
            }

            return false;
        });

        replyMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sendButton.setEnabled(!charSequence.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        myStatusLayout = findViewById(R.id.myStatusLayout);

        if (getIntent() != null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            float[] latLng = intent.getFloatArrayExtra("latLng");
            replyingToMapBubble = new MapBubble(new LatLng(latLng[0], latLng[1]), intent.getStringExtra("name"), intent.getStringExtra("status"));
            ((TextView) replyLayout.findViewById(R.id.replyLayoutName)).setText(replyingToMapBubble.getName());
            ((TextView) replyLayout.findViewById(R.id.replyLayoutStatus)).setText(replyingToMapBubble.getStatus());
            centerMap(replyingToMapBubble.getLatLng());
            showReplyLayout(true);
        }
    }

    private void centerMap(LatLng latLng) {
        if (map == null) {
            centerOnMapLoad = latLng;
            return;
        }

        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        bubbleMapLayer.attach(map, findViewById(R.id.bubbleMapLayer), mapBubble -> {
            ((TextView) replyLayout.findViewById(R.id.replyLayoutName)).setText(mapBubble.getName());
            ((TextView) replyLayout.findViewById(R.id.replyLayoutStatus)).setText(mapBubble.getStatus());
            replyingToMapBubble = mapBubble;
            showReplyLayout(true);
        });

        enableMyLocation();

        map.setOnCameraMoveListener(bubbleMapLayer::update);
        map.setOnCameraIdleListener(bubbleMapLayer::update);
        map.setOnMapClickListener(latlng -> showReplyLayout(false));
        findViewById(R.id.map).addOnLayoutChangeListener((v, i1, i2, i3, i4, i5, i6, i7, i8) -> bubbleMapLayer.update());
        bubbleMapLayer.update();

        if (centerOnMapLoad != null) {
            map.moveCamera(CameraUpdateFactory.newLatLng(centerOnMapLoad));
            centerOnMapLoad = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (Objects.equals(permissions[i], Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (replyLayout.getVisibility() != View.GONE) {
            showReplyLayout(false);
            return;
        }
        super.onBackPressed();
    }

    private void showReplyLayout(boolean show) {
        if (show) {
            if (replyLayout.getVisibility() == View.VISIBLE && (replyLayout.getAnimation() == null || (replyLayout.getAnimation() != null && replyLayout.getAnimation().getDuration() == 195))) {
                return;
            }
        } else {
            if (replyLayout.getVisibility() == View.GONE) {
                return;
            }
        }

        replyLayout.setVisibility(View.VISIBLE);
        replyMessage.setText("");
        sendButton.setEnabled(false);
        Animation animation;

        replyLayout.clearAnimation();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) replyLayout.getLayoutParams();
        int totalHeight = replyLayout.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;

        if (show) {
            animation = new TranslateAnimation(0, 0, -totalHeight, 0);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(195);
            replyLayout.post(() -> {
                replyMessage.requestFocus();
                showKeyboard(replyMessage, true);
            });
        } else {
            animation = new TranslateAnimation(0, 0, replyLayout.getTranslationY(), -totalHeight);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    replyLayout.setVisibility(View.GONE);
                    replyLayout.setAnimation(null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(225);
        }

        replyLayout.startAnimation(animation);

        myStatusLayout.setVisibility(View.VISIBLE);

        if (!show) {
            animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        } else {
            animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    myStatusLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        animation.setDuration(45);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());

        myStatusLayout.startAnimation(animation);
    }

    private void showKeyboard(View view, boolean show) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        if (inputMethodManager == null) {
            return;
        }

        if(show) {
            inputMethodManager.showSoftInput(view, 0);
        } else {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_CODE_PERMISSION);
            }
        } else {
            map.setMyLocationEnabled(true);
            fusedLocationProvider.getLastLocation().addOnCompleteListener(task -> {
                if (task.getResult() == null) {
                    waitForLocation();
                    return;
                }

                onLocationFound(task.getResult());
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void waitForLocation() {
        LocationRequest locationRequest = new LocationRequest()
                .setExpirationDuration(5000)
                .setNumUpdates(1);

        fusedLocationProvider.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    return;
                }

                onLocationFound(locationResult.getLastLocation());
            }
        }, getMainLooper());
    }

    private void onLocationFound(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 13)));
        findViewById(R.id.bubbleMapLayer).postDelayed(() -> {
            bubbleMapLayer.add(new MapBubble(new LatLng(latLng.latitude - .01, latLng.longitude), "Alfred", "Walking the doggo"));
            bubbleMapLayer.add(new MapBubble(new LatLng(latLng.latitude + .01, latLng.longitude + .02), "Meghan", "Homework"));
        }, 1000);
    }

    private void showNotification(MapBubble mapBubble) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }

        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(getString(R.string.reply))
                .build();

        Intent intent = new Intent(this, MapsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("latLng", new float[] { (float) mapBubble.getLatLng().latitude, (float) mapBubble.getLatLng().longitude });
        intent.putExtra("name", mapBubble.getName());
        intent.putExtra("status", mapBubble.getStatus());

        PendingIntent contentIntent = PendingIntent.getActivity(this, REQUEST_CODE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent replyPendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(),
                        REQUEST_CODE_NOTIFICATION,
                        new Intent(getApplicationContext(), Background.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_launcher_foreground,
                        getString(R.string.reply), replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        Notification newMessageNotification =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(mapBubble.getName())
                        .setContentText(mapBubble.getStatus())
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent)
                        .addAction(action)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, newMessageNotification);
    }
}
