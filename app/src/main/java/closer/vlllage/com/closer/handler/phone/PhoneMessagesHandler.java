package closer.vlllage.com.closer.handler.phone;

import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.map.MapActivityHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.ui.CircularRevealActivity;

public class PhoneMessagesHandler extends PoolMember {
    public void openMessagesWithPhone(String phoneId, String name, String status) {
        Runnable runnable = () ->
                $(MapActivityHandler.class).replyToPhone(phoneId, name, status, null);

        if ($(ActivityHandler.class).getActivity() instanceof CircularRevealActivity) {
            ((CircularRevealActivity) $(ActivityHandler.class).getActivity()).finish(runnable);
        } else {
            runnable.run();
        }
    }
}
