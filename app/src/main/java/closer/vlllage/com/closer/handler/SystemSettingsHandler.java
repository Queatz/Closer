package closer.vlllage.com.closer.handler;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import closer.vlllage.com.closer.pool.PoolMember;

public class SystemSettingsHandler extends PoolMember {
    public void showSystemSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", $(ApplicationHandler.class).getApp().getPackageName(), null);
        intent.setData(uri);
        $(ActivityHandler.class).getActivity().startActivity(intent);
    }
}
