package closer.vlllage.com.closer.handler.phone

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On

class LifestyleHandler(private val on: On) {
    fun show(name: String, phone: Phone? = null) {
        on<MenuHandler>().show(
                MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_this_lifestyle) {
                    on<LifestyleHandler>().addLifestyle(name, phone?.id)
                },
                MenuHandler.MenuOption(R.drawable.ic_group_black_24dp, R.string.see_people_with_lifestyle) {
                    on<PhoneListActivityTransitionHandler>().showPhonesForLifestyle(name)
                },
                MenuHandler.MenuOption(R.drawable.ic_message_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.cheer_them, on<NameHandler>().getName(phone))) {
                    on<ReplyHandler>().reply(phone!!)
                }.visible(phone != null)
        )
    }

    fun addLifestyle(name: String, phoneId: String? = null) {
        on<DisposableHandler>().add(on<ApiHandler>().addLifestyle(name)
                .subscribe({
                    on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.added_lifestyle, name))

                    if (phoneId != null) {
                        on<RefreshHandler>().refreshPhone(phoneId)
                    }
                }, { on<DefaultAlerts>().thatDidntWork() }))
    }
}