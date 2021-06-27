package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.handler.bubble.BubbleHandler
import closer.vlllage.com.closer.handler.bubble.MapBubble
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.AccountHandler.Companion.ACCOUNT_FIELD_ACTIVE
import closer.vlllage.com.closer.handler.data.AccountHandler.Companion.ACCOUNT_FIELD_GEO
import closer.vlllage.com.closer.handler.data.LocationHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.TimeStr
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.on.On

class MyBubbleHandler constructor(private val on: On) {

    private var myBubble: MapBubble? = null

    fun updateFrom(accountChange: AccountHandler.AccountChange) {
        when (accountChange.prop) {
            ACCOUNT_FIELD_GEO -> updateLocation(accountChange.value as LatLng)
            ACCOUNT_FIELD_ACTIVE -> {
                val location = on<LocationHandler>().lastKnownLocation
                if (location != null) {
                    updateLocation(LatLng(location.latitude, location.latitude))
                }
                updateActive(accountChange.value as Boolean)
            }
            else -> update()
        }
    }

    private fun updateActive(active: Boolean) {
        if (myBubble == null) {
            return
        }

        if (active) {
            on<BubbleHandler>().add(myBubble!!)
        } else {
            on<BubbleHandler>().remove(myBubble!!)
        }
    }

    private fun updateLocation(latLng: LatLng) {
        if (myBubble == null) {
            val phone = on<StoreHandler>().store.box(Phone::class).query().equal(Phone_.id, on<PersistenceHandler>().phoneId!!).build().findFirst()
            myBubble = MapBubble(
                    latLng,
                    on<AccountHandler>().name,
                    on<AccountHandler>().status)
            myBubble!!.isPinned = true

            if (phone != null) {
                myBubble!!.phone = phone.id
                myBubble!!.tag = phone
                myBubble!!.action = on<TimeStr>().pretty(phone.updated)
            }
            updateActive(on<AccountHandler>().active)
        } else {
            on<BubbleHandler>().move(myBubble!!, latLng)
        }
    }

    private fun update() {
        if (myBubble == null) {
            return
        }

        myBubble!!.name = on<AccountHandler>().name
        myBubble!!.status = on<AccountHandler>().status
        on<BubbleHandler>().updateDetails(myBubble!!)
    }

    fun start() {
        on<DisposableHandler>().add(on<AccountHandler>().changes().subscribe({ this.updateFrom(it) }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }
}
