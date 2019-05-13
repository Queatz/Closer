package closer.vlllage.com.closer.handler.data

import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Icon
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.models.Group
import java.util.*

class AppShortcutsHandler : PoolMember() {
    fun setGroupShortcuts(groups: List<Group>) {
        var groups = groups
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return
        }

        if (groups.size > 3) {
            groups = groups.subList(0, 3)
        }

        val shortcutManager = `$`(ApplicationHandler::class.java).app.getSystemService(ShortcutManager::class.java)
                ?: return

        val shortcuts = ArrayList<ShortcutInfo>()
        val bitmaps = HashSet<Bitmap>()

        for (group in groups) {
            if (group.name == null || group.name!!.isEmpty()) {
                continue
            }

            val icon = `$`(ShortcutIconGenerator::class.java).generate(
                    `$`(Val::class.java).of(group.name, `$`(ResourcesHandler::class.java).resources.getString(R.string.app_name)),
                    128f,
                    Color.WHITE,
                    `$`(GroupColorHandler::class.java).getColor(group),
                    `$`(GroupColorHandler::class.java).getLightColor(group)
            )

            val shortcut = ShortcutInfo.Builder(`$`(ApplicationHandler::class.java).app, "group:" + group.id!!)
                    .setShortLabel(group.name!!)
                    .setIntent(`$`(GroupActivityTransitionHandler::class.java).getIntent(group.id!!, false))
                    .setIcon(Icon.createWithBitmap(icon))
                    .build()

            shortcuts.add(0, shortcut)
            bitmaps.add(icon)
        }

        shortcutManager.dynamicShortcuts = shortcuts

        for (bitmap in bitmaps) {
            bitmap.recycle()
        }
    }
}
