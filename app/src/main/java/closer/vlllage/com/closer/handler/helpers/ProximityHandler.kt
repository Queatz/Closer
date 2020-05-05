package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On

class ProximityHandler constructor(private val on: On) {

    fun findGroupsNear(latLng: LatLng, includeNonHubs: Boolean = false): List<Group> {
        val distance = 0.01714 * 0.125 // 1/8th of a mile

        return on<StoreHandler>().store.box(Group::class).query()
                .between(Group_.latitude, latLng.latitude - distance, latLng.latitude + distance)
                .and()
                .between(Group_.longitude, latLng.longitude - distance, latLng.longitude + distance)
                .and()
                .equal(Group_.physical, true)
                .let {
                    if (!includeNonHubs) {
                        it.notEqual(Group_.name, "")
                    }
                    it
                }
                .sort(on<SortHandler>().sortGroupsByDistance(latLng))
                .build()
                .find()
    }

    fun locationFromLatLng(latLng: LatLng, callback: (String?) -> Unit) {
        val nearestGroupName = on<ProximityHandler>().findGroupsNear(latLng, true).firstOrNull()?.name

        if (nearestGroupName.isNullOrBlank().not()) {
            callback("${on<ResourcesHandler>().resources.getString(R.string.at)} $nearestGroupName")
        } else {
            on<LocalityHelper>().getLocality(latLng) {
                if (it != null) {
                    callback("${on<ResourcesHandler>().resources.getString(R.string.near)} $it")
                } else {
                    callback(null)
                }
            }
        }
    }
}