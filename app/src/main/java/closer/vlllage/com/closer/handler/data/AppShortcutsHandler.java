package closer.vlllage.com.closer.handler.data;

import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
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

        for (Group group : groups) {
            if (group.getName() == null || group.getName().isEmpty()) {
                continue;
            }

            ShortcutInfo shortcut = new ShortcutInfo.Builder($(ApplicationHandler.class).getApp(), "group:" + group.getId())
                    .setShortLabel(group.getName())
                    .setIntent($(GroupActivityTransitionHandler.class).getIntent(group.getId()))
                    .setIcon(Icon.createWithResource($(ApplicationHandler.class).getApp(), R.drawable.ic_notification))
                    .build();

            shortcuts.add(0, shortcut);
        }



        shortcutManager.setDynamicShortcuts(shortcuts);
    }
}
