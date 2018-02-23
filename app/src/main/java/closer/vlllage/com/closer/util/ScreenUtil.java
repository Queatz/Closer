package closer.vlllage.com.closer.util;

import android.content.Context;
import android.os.PowerManager;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/**
 * Created by jacob on 2/19/18.
 */

public class ScreenUtil {
    public static void ensureScreenIsOn(Context context) {
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);

        if(pm != null && !pm.isInteractive()) {
            PowerManager.WakeLock wl = pm.newWakeLock(FLAG_KEEP_SCREEN_ON | PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"closer:notification");
            wl.acquire(5000);
        }
    }
}
