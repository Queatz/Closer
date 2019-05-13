package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.pool.PoolMember
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class MapZoomHandler : PoolMember() {

    private val onZoomChanged = BehaviorSubject.create<Float>()

    fun update(zoom: Float) {
        onZoomChanged.onNext(zoom)
    }

    fun onZoomGreaterThanChanged(zoomGreaterThan: Float): Observable<Boolean> {
        return onZoomChanged.map { zoom -> zoom >= zoomGreaterThan }.distinctUntilChanged()
    }
}
