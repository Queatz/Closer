package closer.vlllage.com.closer.handler.helpers;

import android.app.Service;
import android.graphics.Rect;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class KeyboardHandler extends PoolMember {

    private static final int KEYBOARD_DELAY_MS = 500;

    public void showKeyboard(View view, boolean show) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext()
                .getSystemService(Service.INPUT_METHOD_SERVICE);

        if (inputMethodManager == null) {
            return;
        }

        if(show) {
            inputMethodManager.showSoftInput(view, 0);
        } else {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showViewAboveKeyboard(View view) {
        $(TimerHandler.class).postDisposable(() -> {
            Rect rect = new Rect();
            view.getLocalVisibleRect(rect);
            rect.bottom += $(KeyboardVisibilityHandler.class).getLastKeyboardHeight();
            rect.bottom += $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.padDouble);
            view.requestRectangleOnScreen(rect);
        }, KEYBOARD_DELAY_MS);
    }
}
