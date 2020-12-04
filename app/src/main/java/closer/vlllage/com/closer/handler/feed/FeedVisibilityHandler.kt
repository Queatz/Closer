package closer.vlllage.com.closer.handler.feed

import com.queatz.on.On
import io.reactivex.subjects.PublishSubject

class FeedVisibilityHandler(private val on: On) {

    val positionOnScreen = PublishSubject.create<Int>()

    fun onScreen(position: Int) {
        positionOnScreen.onNext(position)
    }

}
