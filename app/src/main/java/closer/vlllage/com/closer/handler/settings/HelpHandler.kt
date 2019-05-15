package closer.vlllage.com.closer.handler.settings

import android.text.Html

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.queatz.on.On

class HelpHandler constructor(private val on: On) {
    fun showHelp() {
        on<DefaultAlerts>().longMessage(null, Html.fromHtml(on<ResourcesHandler>().resources.getString(R.string.help_message_html)))
    }
}
