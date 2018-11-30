package closer.vlllage.com.closer.handler.data;

import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Icon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.GroupColorHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.ShortcutIconGenerator;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Group;

public class AppShortcutsHandler extends PoolMember {
    public void setGroupShortcuts(List<Group> groups) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        if (groups.size() > 3) {
            groups = groups.subList(0, 3);
        }

        ShortcutManager shortcutManager = $(ApplicationHandler.class).getApp().getSystemService(ShortcutManager.class);

        if (shortcutManager == null) {
            return;
        }

        List<ShortcutInfo> shortcuts = new ArrayList<>();
        Set<Bitmap> bitmaps = new HashSet<>();

        for (Group group : groups) {
            if (shortcuts.size() >= 3) {
                break;
            }

            if (group.getName() == null || group.getName().isEmpty()) {
                continue;
            }

            Bitmap icon = $(ShortcutIconGenerator.class).generate(
                    $(Val.class).of(group.getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)),
                    128,
                    Color.WHITE,
                    $(GroupColorHandler.class).getColor(group),
                    $(GroupColorHandler.class).getLightColor(group)
            );

            ShortcutInfo shortcut = new ShortcutInfo.Builder($(ApplicationHandler.class).getApp(), "group:" + group.getId())
                    .setShortLabel(group.getName())
                    .setIntent($(GroupActivityTransitionHandler.class).getIntent(group.getId(), false))
                    .setIcon(Icon.createWithBitmap(icon))
                    .build();

            shortcuts.add(0, shortcut);
            bitmaps.add(icon);
        }

        shortcutManager.setDynamicShortcuts(shortcuts);

        for (Bitmap bitmap : bitmaps) {
            bitmap.recycle();
        }
    }
}
