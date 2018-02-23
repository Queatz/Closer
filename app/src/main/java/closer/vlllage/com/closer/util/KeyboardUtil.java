package closer.vlllage.com.closer.util;

import android.app.Service;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtil {
    public static void showKeyboard(View view, boolean show) {
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
}
