package closer.vlllage.com.closer.handler.phone

import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers

class ReplyHandler constructor(private val on: On) {
    fun reply(phone: Phone) {
        reply(phone.id!!, on<NameHandler>().getName(phone))
    }

    private fun reply(phoneId: String, phoneName: String? = null) {
        on<DataHandler>().getDirectGroup(phoneId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            on<GroupActivityTransitionHandler>().showGroupMessages(null, it.id!!, true)
        }, {
            on<DefaultAlerts>().thatDidntWork()
        }).also {
            on<DisposableHandler>().add(it)
        }
    }
}