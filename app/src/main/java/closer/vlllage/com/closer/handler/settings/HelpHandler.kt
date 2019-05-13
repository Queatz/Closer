package closer.vlllage.com.closer.handler.settings

import android.text.Html

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolMember

class HelpHandler : PoolMember() {
    fun showHelp() {
        `$`(DefaultAlerts::class.java).longMessage(null, Html.fromHtml(`$`(ResourcesHandler::class.java).resources.getString(R.string.help_message_html)))
    }
}
