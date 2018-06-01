package closer.vlllage.com.closer.handler.helpers;

import android.view.View;

import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class KeyboardVisibilityHandler extends PoolMember {

    private BehaviorSubject<Boolean> isKeyboardVisibleObservable = BehaviorSubject.createDefault(false);

    public void attach(View viewFitsSystemWindows) {
        viewFitsSystemWindows.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            isKeyboardVisibleObservable.onNext(viewFitsSystemWindows.getPaddingBottom() != 0);
        });
    }

    public Observable<Boolean> isKeyboardVisible() {
        return isKeyboardVisibleObservable.distinctUntilChanged();
    }
}
