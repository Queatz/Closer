package closer.vlllage.com.closer.handler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import closer.vlllage.com.closer.pool.PoolMember;

public class PersistenceHandler extends PoolMember {

    private static final String SHARED_PREFERENCES = "closer.prefs";
    private static final String PREFERENCE_MY_STATUS = "closer.me.status";
    private static final String PREFERENCE_MY_NAME = "closer.me.name";
    private static final String PREFERENCE_MY_ACTIVE = "closer.me.active";
    private static final String PREFERENCE_DEVICE_TOKEN = "closer.device-token";
    private static final String PREFERENCE_PHONE = "closer.phone";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onPoolInit() {
        sharedPreferences = $(ActivityHandler.class).getActivity().getSharedPreferences(
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
}
