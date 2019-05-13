package closer.vlllage.com.closer.handler.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_SCREEN_MAP
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.WindowHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting.CLOSER_SETTINGS_OPEN_GROUP_EXPANDED
import closer.vlllage.com.closer.pool.PoolFragment
import closer.vlllage.com.closer.ui.Animate

class SettingsSlideFragment : PoolFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        `$`(ApiHandler::class.java).setAuthorization(`$`(AccountHandler::class.java).phone)

        val view = inflater.inflate(R.layout.activity_settings, container, false)
        view.findViewById<View>(R.id.scrollView).setPadding(0, `$`(WindowHandler::class.java).statusBarHeight, 0, 0)

        val openGroupsExpandedSettingsSwitch = view.findViewById<Switch>(R.id.openGroupsExpandedSettingsSwitch)
        openGroupsExpandedSettingsSwitch.isChecked = `$`(SettingsHandler::class.java).get(CLOSER_SETTINGS_OPEN_GROUP_EXPANDED)
        openGroupsExpandedSettingsSwitch.setOnCheckedChangeListener { v, checked -> `$`(SettingsHandler::class.java).set(CLOSER_SETTINGS_OPEN_GROUP_EXPANDED, checked) }

        view.findViewById<View>(R.id.sendFeedbackButton).setOnClickListener { v -> `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(v, `$`(ConfigHandler::class.java).feedbackGroupId()) }
        view.findViewById<View>(R.id.viewPrivacyPolicyButton).setOnClickListener { v -> `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).privacy().subscribe({ privacyPolicy -> `$`(DefaultAlerts::class.java).message(privacyPolicy) }, { e -> `$`(DefaultAlerts::class.java).thatDidntWork() })) }
        view.findViewById<View>(R.id.showHelpButton).setOnClickListener { v -> `$`(HelpHandler::class.java).showHelp() }
        view.findViewById<View>(R.id.returnToMapButton).setOnClickListener { v -> `$`(MapActivityHandler::class.java).goToScreen(EXTRA_SCREEN_MAP) }

        val publicNotificationsSettingsSwitch = view.findViewById<Switch>(R.id.publicNotificationsSettingsSwitch)
        val publicNotificationsSettingsSwitchDescription = view.findViewById<View>(R.id.publicNotificationsSettingsSwitchDescription)

        publicNotificationsSettingsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            `$`(Animate::class.java).alpha(publicNotificationsSettingsSwitchDescription, !isChecked)
            `$`(AccountHandler::class.java).updatePrivateMode(!isChecked)
        }

        publicNotificationsSettingsSwitch.isChecked = !`$`(AccountHandler::class.java).privateMode

        return view
    }

}
