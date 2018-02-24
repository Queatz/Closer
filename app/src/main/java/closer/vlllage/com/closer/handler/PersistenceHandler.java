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
}
