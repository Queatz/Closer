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
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import com.google.android.gms.maps.model.LatLng

class MyBubbleHandler : PoolMember() {

    var myBubble: MapBubble? = null
        private set

    fun updateFrom(accountChange: AccountHandler.AccountChange) {
        when (accountChange.prop) {
            ACCOUNT_FIELD_GEO -> updateLocation(accountChange.value as LatLng)
            ACCOUNT_FIELD_ACTIVE -> {
                val location = `$`(LocationHandler::class.java).lastKnownLocation
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
            `$`(BubbleHandler::class.java).add(myBubble!!)
        } else {
            `$`(BubbleHandler::class.java).remove(myBubble!!)
        }
    }

    private fun updateLocation(latLng: LatLng) {
        if (myBubble == null) {
            val phone = `$`(StoreHandler::class.java).store.box(Phone::class.java).query().equal(Phone_.id, `$`(PersistenceHandler::class.java).phoneId!!).build().findFirst()
            myBubble = MapBubble(latLng, `$`(AccountHandler::class.java).name, `$`(AccountHandler::class.java).status)
            myBubble!!.isPinned = true

            if (phone != null) {
                myBubble!!.tag = phone
                myBubble!!.action = `$`(TimeStr::class.java).pretty(phone.updated)
            }
            updateActive(`$`(AccountHandler::class.java).active)
        } else {
            `$`(BubbleHandler::class.java).move(myBubble!!, latLng)
        }
    }

    private fun update() {
        if (myBubble == null) {
            return
        }

        myBubble!!.name = `$`(AccountHandler::class.java).name
        myBubble!!.status = `$`(AccountHandler::class.java).status
        `$`(BubbleHandler::class.java).updateDetails(myBubble!!)
    }

    fun start() {
        `$`(DisposableHandler::class.java).add(`$`(AccountHandler::class.java).changes().subscribe({ this.updateFrom(it) }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    fun isMyBubble(mapBubble: MapBubble): Boolean {
        return myBubble == mapBubble
    }
}
