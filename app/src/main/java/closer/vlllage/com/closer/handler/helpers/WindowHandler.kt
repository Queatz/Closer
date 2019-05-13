package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember

class WindowHandler : PoolMember() {
    val statusBarHeight: Int
        get() {
            val statusBarHeightResId = `$`(ResourcesHandler::class.java).resources.getIdentifier("status_bar_height", "dimen", "android")

            return if (statusBarHeightResId == 0) {
                0
            } else `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(statusBarHeightResId)

        }
}
