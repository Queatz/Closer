package closer.vlllage.com.closer.handler.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class PersistenceHandler extends PoolMember {

    private static final String SHARED_PREFERENCES = "closer.prefs";
    private static final String PREFERENCE_MY_STATUS = "closer.me.status";
    private static final String PREFERENCE_MY_NAME = "closer.me.name";
    private static final String PREFERENCE_MY_ACTIVE = "closer.me.active";
    private static final String PREFERENCE_DEVICE_TOKEN = "closer.device-token";
    private static final String PREFERENCE_PHONE = "closer.phone";
    private static final String PREFERENCE_VERIFIED = "closer.verified";
    private static final String PREFERENCE_PHONE_ID = "closer.phone.id";
    private static final String PREFERENCE_HELP_IS_HIDDEN = "closer.state.help-is-hidden";
    private static final String PREFERENCE_NOTIFICATIONS_PAUSED = "closer.notifications.paused";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onPoolInit() {
        sharedPreferences = $(ApplicationHandler.class).getApp().getSharedPreferences(
                SHARED_PREFERENCES, Context.MODE_PRIVATE
        );
    }

    public String getMyStatus() {
        return sharedPreferences.getString(PREFERENCE_MY_STATUS, "");
    }

    public String getMyName() {
        return sharedPreferences.getString(PREFERENCE_MY_NAME, "");
    }

    public boolean getMyActive() {
        return sharedPreferences.getBoolean(PREFERENCE_MY_ACTIVE, false);
    }

    public String getDeviceToken() {
        return sharedPreferences.getString(PREFERENCE_DEVICE_TOKEN, null);
    }

    public String getPhone() {
        return sharedPreferences.getString(PREFERENCE_PHONE, null);
    }

    public boolean getIsVerified() {
        return sharedPreferences.getBoolean(PREFERENCE_VERIFIED, false);
    }

    public String getPhoneId() {
        return sharedPreferences.getString(PREFERENCE_PHONE_ID, null);
    }

    public boolean getIsHelpHidden() {
        return sharedPreferences.getBoolean(PREFERENCE_HELP_IS_HIDDEN, false);
    }

    public boolean getIsNotifcationsPaused() {
        return sharedPreferences.getBoolean(PREFERENCE_NOTIFICATIONS_PAUSED, false);
    }

    @SuppressLint("ApplySharedPref")
    public void setMyStatus(String status) {
        sharedPreferences.edit().putString(PREFERENCE_MY_STATUS, status).commit();
    }

    @SuppressLint("ApplySharedPref")
    public void setMyName(String name) {
        sharedPreferences.edit().putString(PREFERENCE_MY_NAME, name).commit();
    }

    @SuppressLint("ApplySharedPref")
    public void setMyActive(boolean active) {
        sharedPreferences.edit().putBoolean(PREFERENCE_MY_ACTIVE, active).commit();
    }

    @SuppressLint("ApplySharedPref")
    public void setDeviceToken(String deviceToken) {
        sharedPreferences.edit().putString(PREFERENCE_DEVICE_TOKEN, deviceToken).commit();
    }

    @SuppressLint("ApplySharedPref")
    public void setPhone(String phone) {
        sharedPreferences.edit().putString(PREFERENCE_PHONE, phone).commit();
    }

    @SuppressLint("ApplySharedPref")
    public void setIsVerified(boolean verified) {
        sharedPreferences.edit().putBoolean(PREFERENCE_VERIFIED, verified).commit();
    }

    @SuppressLint("ApplySharedPref")
    public void setPhoneId(String phoneId) {
        sharedPreferences.edit().putString(PREFERENCE_PHONE_ID, phoneId).commit();
    }

    @SuppressLint("ApplySharedPref")
    public void setIsHelpHidden(boolean helpIsHidden) {
        sharedPreferences.edit().putBoolean(PREFERENCE_HELP_IS_HIDDEN, helpIsHidden).commit();
    }

    @SuppressLint("ApplySharedPref")
    public void setIsNotificationsPaused(boolean isNotificationsPaused) {
        sharedPreferences.edit().putBoolean(PREFERENCE_NOTIFICATIONS_PAUSED, isNotificationsPaused).commit();
    }
}
