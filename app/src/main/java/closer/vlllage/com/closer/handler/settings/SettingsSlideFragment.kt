package closer.vlllage.com.closer.handler.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import closer.vlllage.com.closer.BuildConfig
import closer.vlllage.com.closer.MapsActivity
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.featurerequests.FeatureRequestsHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.map.NetworkConnectionViewHandler
import closer.vlllage.com.closer.pool.PoolFragment
import closer.vlllage.com.closer.ui.Animate
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsSlideFragment : PoolFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_settings, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        on<NetworkConnectionViewHandler>().attach(connectionError)

        openGroupsExpandedSettingsSwitch.isChecked = on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_OPEN_GROUP_EXPANDED]
        openGroupsExpandedSettingsSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_OPEN_GROUP_EXPANDED] = checked }

        lightThemeSwitch.isChecked = on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME]
        lightThemeSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME] = checked }

        autoAnswerSwitch.isChecked = on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_AUTO_ANSWER_CALLS]
        autoAnswerSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_AUTO_ANSWER_CALLS] = checked }

        showCloseButtonSettingsSwitch.isChecked = !on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_HIDE_CLOSE_BUTTON]
        showCloseButtonSettingsSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_HIDE_CLOSE_BUTTON] = !checked }

        sendFeedbackButton.setOnClickListener { v -> on<GroupActivityTransitionHandler>().showGroupMessages(v, on<ConfigHandler>().feedbackGroupId()) }
        viewPrivacyPolicyButton.setOnClickListener { on<DisposableHandler>().add(on<ApiHandler>().privacy().subscribe({ privacyPolicy -> on<DefaultAlerts>().message(privacyPolicy) }, { e -> on<DefaultAlerts>().thatDidntWork() })) }
        viewTermsOfUseButton.setOnClickListener { on<DisposableHandler>().add(on<ApiHandler>().terms().subscribe({ terms -> on<DefaultAlerts>().message(terms) }, { e -> on<DefaultAlerts>().thatDidntWork() })) }
        returnToMapButton.setOnClickListener { on<MapActivityHandler>().goToScreen(MapsActivity.EXTRA_SCREEN_MAP) }

        val publicNotificationsSettingsSwitch = view.findViewById<Switch>(R.id.publicNotificationsSettingsSwitch)
        val publicNotificationsSettingsSwitchDescription = publicNotificationsSettingsSwitchDescription

        publicNotificationsSettingsSwitch.setOnCheckedChangeListener { _, isChecked ->
            on<Animate>().alpha(publicNotificationsSettingsSwitchDescription, !isChecked)
            on<AccountHandler>().updatePrivateMode(!isChecked)
        }

        publicNotificationsSettingsSwitch.isChecked = !on<AccountHandler>().privateMode
        publicNotificationsSettingsSwitchDescription.visible = !publicNotificationsSettingsSwitch.isChecked

        featureRequestsButton.setOnClickListener {
            on<FeatureRequestsHandler>().show()
        }

        kioskModeButton.setOnClickListener {
            on<DefaultAlerts>().message("Coming soon!")
        }

        appVersion.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

//        val privateModeSettingsSwitch = view.findViewById<Switch>(R.id.privateModeSettingsSwitch)
//        val privateModeSettingsSwitchDescription = privateModeSettingsSwitchDescription
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
