package closer.vlllage.com.closer.handler.helpers;

import android.graphics.Color;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Group;

public class InstallShortcutHandler extends PoolMember {
    public void installShortcut(Group group) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported($(ApplicationHandler.class).getApp())) {
            ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder($(ApplicationHandler.class).getApp(), "group-" + group.getId())
                    .setIntent($(GroupActivityTransitionHandler.class).getIntent(group.getId(), false))
                    .setShortLabel($(Val.class).of(group.getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)))
                    .setIcon(IconCompat.createWithBitmap($(ShortcutIconGenerator.class).generate(
                            $(Val.class).of(group.getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)).substring(0, 2),
                            128,
                            Color.WHITE,
                            $(GroupColorHandler.class).getColor(group),
                            $(GroupColorHandler.class).getLightColor(group)
                    )))
                    .build();
            ShortcutManagerCompat.requestPinShortcut($(ApplicationHandler.class).getApp(), shortcutInfo, null);
        }
    }
}
