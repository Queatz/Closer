package closer.vlllage.com.closer.handler.map;

import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class MapZoomHandler extends PoolMember {

    private BehaviorSubject<Float> onZoomChanged = BehaviorSubject.create();

    public void update(float zoom) {
        onZoomChanged.onNext(zoom);
    }

    public Observable<Boolean> onZoomGreaterThanChanged(final float zoomGreaterThan) {
        return onZoomChanged.map(zoom -> zoom >= zoomGreaterThan).distinctUntilChanged();
    }
}
