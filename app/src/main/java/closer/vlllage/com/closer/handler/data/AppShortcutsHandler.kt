package closer.vlllage.com.closer.handler.data

import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Icon
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.*
import com.queatz.on.On
import closer.vlllage.com.closer.store.models.Group
import java.util.*

class AppShortcutsHandler constructor(private val on: On) {
    fun setGroupShortcuts(groups: List<Group>) {
        var groups = groups
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return
        }

        if (groups.size > 3) {
            groups = groups.subList(0, 3)
        }

        val shortcutManager = on<ApplicationHandler>().app.getSystemService(ShortcutManager::class.java)
                ?: return

        val shortcuts = ArrayList<ShortcutInfo>()
        val bitmaps = HashSet<Bitmap>()

        for (group in groups) {
            if (group.name?.isEmpty() == false) {
                continue
            }

            val icon = on<ShortcutIconGenerator>().generate(
                    on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.app_name)),
                    128f,
                    Color.WHITE,
                    on<GroupColorHandler>().getColor(group),
                    on<GroupColorHandler>().getLightColor(group)
            )

            val shortcut = ShortcutInfo.Builder(on<ApplicationHandler>().app, "group:" + group.id!!)
                    .setShortLabel(if (group.name.isNullOrBlank()) on<ResourcesHandler>().resources.getString(R.string.app_name) else group.name!!)
                    .setIntent(on<GroupActivityTransitionHandler>().getIntent(group.id!!, false))
                    .setIcon(Icon.createWithBitmap(icon))
                    .build()

            shortcuts.add(0, shortcut)
            bitmaps.add(icon)
        }

        shortcutManager.dynamicShortcuts = shortcuts

        bitmaps.forEach { it.recycle() }
    }
}
