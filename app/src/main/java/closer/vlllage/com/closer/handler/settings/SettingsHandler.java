package closer.vlllage.com.closer.handler.settings;

import android.content.Context;
import android.content.SharedPreferences;

import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class SettingsHandler extends PoolMember {

    private static final String SHARED_PREFERENCES = "closer.user.settings";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onPoolInit() {
        sharedPreferences = $(ApplicationHandler.class).getApp().getSharedPreferences(
                SHARED_PREFERENCES, Context.MODE_PRIVATE
        );
    }

    public boolean get(UserLocalSetting userLocalSetting) {
        return sharedPreferences.getBoolean(userLocalSetting.name(), false);
    }

    public void set(UserLocalSetting userLocalSetting, boolean newValue) {
        sharedPreferences.edit().putBoolean(userLocalSetting.name(), newValue).apply();
    }
}
