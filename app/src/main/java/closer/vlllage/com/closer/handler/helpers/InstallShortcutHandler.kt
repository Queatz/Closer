package closer.vlllage.com.closer.handler.helpers

import android.graphics.Color
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On

class InstallShortcutHandler constructor(private val on: On) {
    fun installShortcut(group: Group) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(on<ApplicationHandler>().app)) {
            val name = on<Val>().of(if (group.hasPhone()) on<NameHandler>().getName(group.phoneId!!) else
                group.name, on<ResourcesHandler>().resources.getString(R.string.app_name))
            val icon = on<ShortcutIconGenerator>().generate(
                    name,
                    128f,
                    Color.WHITE,
                    on<GroupColorHandler>().getColor(group),
                    on<GroupColorHandler>().getLightColor(group)
            )
            val shortcutInfo = ShortcutInfoCompat.Builder(on<ApplicationHandler>().app, "group-" + group.id!!)
                    .setIntent(on<GroupActivityTransitionHandler>().getIntent(group.id!!, false))
                    .setShortLabel(name)
                    .setIcon(IconCompat.createWithBitmap(icon))
                    .build()
            ShortcutManagerCompat.requestPinShortcut(on<ApplicationHandler>().app, shortcutInfo, null)
            icon.recycle()
        }
    }
}
