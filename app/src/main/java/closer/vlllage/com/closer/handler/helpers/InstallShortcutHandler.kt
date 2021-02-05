package closer.vlllage.com.closer.handler.helpers

import android.graphics.Color
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers

class InstallShortcutHandler constructor(private val on: On) {
    fun installShortcut(group: Group) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(on<ApplicationHandler>().app)) {
            on<GroupNameHelper>().getName(group)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ name ->
                on<DefaultInput>().show(R.string.icon_text, prefill = name.take(4), maxLength = 4) { iconText ->
                    val icon = on<ShortcutIconGenerator>().generate(
                        iconText,
                        128f,
                        Color.WHITE,
                        on<GroupColorHandler>().getColor(group),
                        on<GroupColorHandler>().getLightColor(group)
                    )
                    val shortcutInfo = ShortcutInfoCompat.Builder(on<ApplicationHandler>().app, "group-" + group.id!!)
                        .setIntent(on<GroupActivityTransitionHandler>().getIntent(group.id!!, false, isPhone = group.hasPhone()))
                        .setShortLabel(name)
                        .setIcon(IconCompat.createWithBitmap(icon))
                        .build()
                    ShortcutManagerCompat.requestPinShortcut(on<ApplicationHandler>().app, shortcutInfo, null)
                    icon.recycle()
                }
            }, {
                on<DefaultAlerts>().thatDidntWork()
            }).also {
                on<DisposableHandler>().add(it)
            }
        }
    }
}
