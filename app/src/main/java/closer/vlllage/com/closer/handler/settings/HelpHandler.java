package closer.vlllage.com.closer.handler.settings;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.pool.PoolMember;

public class HelpHandler extends PoolMember {
    public void showHelp() {
        $(DefaultAlerts.class).longMessage(null, R.string.help_message);
    }
}
