package closer.vlllage.com.closer.handler.settings;

import android.text.Html;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class HelpHandler extends PoolMember {
    public void showHelp() {
        $(DefaultAlerts.class).longMessage(null, Html.fromHtml($(ResourcesHandler.class).getResources().getString(R.string.help_message_html)));
    }
}
