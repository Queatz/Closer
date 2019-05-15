package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On

class WindowHandler constructor(private val on: On) {
    val statusBarHeight: Int
        get() {
            val statusBarHeightResId = on<ResourcesHandler>().resources.getIdentifier("status_bar_height", "dimen", "android")

            return if (statusBarHeightResId == 0)
                0
            else on<ResourcesHandler>().resources.getDimensionPixelSize(statusBarHeightResId)

        }
}
