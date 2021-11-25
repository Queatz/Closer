package closer.vlllage.com.closer.handler.map

import com.queatz.on.On
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class MapZoomHandler constructor(private val on: On) {

    private val onZoomChanged = BehaviorSubject.create<Float>()

    fun update(zoom: Float) {
        onZoomChanged.onNext(zoom)
    }

    fun onZoomGreaterThanChanged(zoomGreaterThan: Float): Observable<Boolean> {
        return onZoomChanged.map { zoom -> zoom >= zoomGreaterThan }.distinctUntilChanged()
    }
}
