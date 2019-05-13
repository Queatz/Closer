package closer.vlllage.com.closer.handler.helpers

import android.app.Service
import android.graphics.Rect
import android.view.View
import android.view.inputmethod.InputMethodManager

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.pool.PoolMember

class KeyboardHandler : PoolMember() {

    fun showKeyboard(view: View, show: Boolean) {
        val inputMethodManager = view.context
                .getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager ?: return

        if (show) {
            inputMethodManager.showSoftInput(view, 0)
        } else {
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showViewAboveKeyboard(view: View) {
        `$`(TimerHandler::class.java).postDisposable(Runnable {
            val rect = Rect()
            view.getLocalVisibleRect(rect)
            rect.bottom += `$`(KeyboardVisibilityHandler::class.java).lastKeyboardHeight
            rect.bottom += `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.padDouble)
            view.requestRectangleOnScreen(rect)
        }, KEYBOARD_DELAY_MS.toLong())
    }

    companion object {

        private val KEYBOARD_DELAY_MS = 500
    }
}
