package closer.vlllage.com.closer.handler.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import closer.vlllage.com.closer.MapsActivity
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.WindowHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.pool.PoolFragment
import closer.vlllage.com.closer.ui.Animate
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsSlideFragment : PoolFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.scrollView).setPadding(0, on<WindowHandler>().statusBarHeight, 0, 0)

        openGroupsExpandedSettingsSwitch.isChecked = on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_OPEN_GROUP_EXPANDED]
        openGroupsExpandedSettingsSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_OPEN_GROUP_EXPANDED] = checked }

        lightThemeSwitch.isChecked = !on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME]
        lightThemeSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME] = !checked }

        showCloseButtonSettingsSwitch.isChecked = !on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_HIDE_CLOSE_BUTTON]
        showCloseButtonSettingsSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_HIDE_CLOSE_BUTTON] = !checked }

        view.findViewById<View>(R.id.sendFeedbackButton).setOnClickListener { v -> on<GroupActivityTransitionHandler>().showGroupMessages(v, on<ConfigHandler>().feedbackGroupId()) }
        view.findViewById<View>(R.id.viewPrivacyPolicyButton).setOnClickListener { on<DisposableHandler>().add(on<ApiHandler>().privacy().subscribe({ privacyPolicy -> on<DefaultAlerts>().message(privacyPolicy) }, { e -> on<DefaultAlerts>().thatDidntWork() })) }
        view.findViewById<View>(R.id.showHelpButton).setOnClickListener { on<HelpHandler>().showHelp() }
        view.findViewById<View>(R.id.returnToMapButton).setOnClickListener { on<MapActivityHandler>().goToScreen(MapsActivity.EXTRA_SCREEN_MAP) }

        val publicNotificationsSettingsSwitch = view.findViewById<Switch>(R.id.publicNotificationsSettingsSwitch)
        val publicNotificationsSettingsSwitchDescription = view.findViewById<View>(R.id.publicNotificationsSettingsSwitchDescription)

        publicNotificationsSettingsSwitch.setOnCheckedChangeListener { _, isChecked ->
            on<Animate>().alpha(publicNotificationsSettingsSwitchDescription, !isChecked)
            on<AccountHandler>().updatePrivateMode(!isChecked)
        }

        publicNotificationsSettingsSwitch.isChecked = !on<AccountHandler>().privateMode
        publicNotificationsSettingsSwitchDescription.visible = !publicNotificationsSettingsSwitch.isChecked

//        val privateModeSettingsSwitch = view.findViewById<Switch>(R.id.privateModeSettingsSwitch)
//        val privateModeSettingsSwitchDescription = view.findViewById<View>(R.id.privateModeSettingsSwitchDescription)
//
//        privateModeSettingsSwitch.setOnCheckedChangeListener { _, isChecked ->
//            on<Animate>().alpha(privateModeSettingsSwitchDescription, isChecked)
//            on<AccountHandler>().updatePrivateOnly(isChecked)
//        }
//
//        privateModeSettingsSwitch.isChecked = on<AccountHandler>().privateOnly
//        privateModeSettingsSwitchDescription.visible = privateModeSettingsSwitch.isChecked
    }
}
