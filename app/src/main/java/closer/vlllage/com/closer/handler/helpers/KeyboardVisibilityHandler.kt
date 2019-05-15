package closer.vlllage.com.closer.handler.helpers

import android.view.View

import com.queatz.on.On
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class KeyboardVisibilityHandler constructor(private val on: On) {

    private val isKeyboardVisibleObservable = BehaviorSubject.createDefault(false)
    var lastKeyboardHeight = 0
        private set

    val isKeyboardVisible: Observable<Boolean>
        get() = isKeyboardVisibleObservable.distinctUntilChanged()

    fun attach(viewFitsSystemWindows: View) {
        viewFitsSystemWindows.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (viewFitsSystemWindows.paddingBottom != 0) {
                lastKeyboardHeight = viewFitsSystemWindows.paddingBottom
            }

            isKeyboardVisibleObservable.onNext(viewFitsSystemWindows.paddingBottom != 0)
        }
    }
}
