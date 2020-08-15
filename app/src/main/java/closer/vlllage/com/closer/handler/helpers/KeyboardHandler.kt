package closer.vlllage.com.closer.handler.helpers

import android.app.Service
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

import closer.vlllage.com.closer.R
import com.queatz.on.On

class KeyboardHandler constructor(private val on: On) {

    fun showKeyboard(view: View, show: Boolean) {
        val inputMethodManager = view.context
                .getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager

        if (show) {
            inputMethodManager.showSoftInput(view, 0)
        } else {
            view.windowToken?.let {
                inputMethodManager.hideSoftInputFromWindow(it, 0)
            } ?: on<ActivityHandler>().activity?.window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            )
        }
    }

    fun showViewAboveKeyboard(view: View) {
        on<TimerHandler>().postDisposable({
            val rect = Rect()
            view.getLocalVisibleRect(rect)
            rect.bottom += on<KeyboardVisibilityHandler>().lastKeyboardHeight
            rect.bottom += on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble)
            view.requestRectangleOnScreen(rect)
        }, KEYBOARD_DELAY_MS.toLong())
    }

    companion object {
        private const val KEYBOARD_DELAY_MS = 500
    }
}
