package closer.vlllage.com.closer.handler.helpers

import android.graphics.Color
import android.support.v4.content.pm.ShortcutInfoCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.support.v4.graphics.drawable.IconCompat
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.models.Group

class InstallShortcutHandler : PoolMember() {
    fun installShortcut(group: Group) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(`$`(ApplicationHandler::class.java).app)) {
            val icon = `$`(ShortcutIconGenerator::class.java).generate(
                    `$`(Val::class.java).of(group.name!!, `$`(ResourcesHandler::class.java).resources.getString(R.string.app_name)),
                    128f,
                    Color.WHITE,
                    `$`(GroupColorHandler::class.java).getColor(group),
                    `$`(GroupColorHandler::class.java).getLightColor(group)
            )
            val shortcutInfo = ShortcutInfoCompat.Builder(`$`(ApplicationHandler::class.java).app, "group-" + group.id!!)
                    .setIntent(`$`(GroupActivityTransitionHandler::class.java).getIntent(group.id!!, false))
                    .setShortLabel(`$`(Val::class.java).of(group.name!!, `$`(ResourcesHandler::class.java).resources.getString(R.string.app_name)))
                    .setIcon(IconCompat.createWithBitmap(icon))
                    .build()
            ShortcutManagerCompat.requestPinShortcut(`$`(ApplicationHandler::class.java).app, shortcutInfo, null)
            icon.recycle()
        }
    }
}
