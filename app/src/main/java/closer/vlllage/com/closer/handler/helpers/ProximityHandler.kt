package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On

class ProximityHandler constructor(private val on: On) {

    fun findGroupsNear(latLng: LatLng): List<Group> {
        val distance = 0.01714 * 0.125 // 1/8th of a mile

        return on<StoreHandler>().store.box(Group::class).query()
                .between(Group_.latitude, latLng.latitude - distance, latLng.latitude + distance)
                .and()
                .between(Group_.longitude, latLng.longitude - distance, latLng.longitude + distance)
                .and()
                .equal(Group_.physical, true)
                .notEqual(Group_.name, "")
                .sort(on<SortHandler>().sortGroups())
                .build()
                .find()
    }
}