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
import closer.vlllage.com.closer.databinding.ActivitySettingsBinding
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

class SettingsSlideFragment : PoolFragment() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ActivitySettingsBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        on<NetworkConnectionViewHandler>().attach(binding.connectionError)

        binding.openGroupsExpandedSettingsSwitch.isChecked = on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_OPEN_GROUP_EXPANDED]
        binding.openGroupsExpandedSettingsSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_OPEN_GROUP_EXPANDED] = checked }

        binding.openFeedExpandedSettingsSwitch.isChecked = on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_OPEN_FEED_EXPANDED]
        binding.openFeedExpandedSettingsSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_OPEN_FEED_EXPANDED] = checked }

        binding.rememberLastTabSettingsSwitch.isChecked = on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_REMEMBER_LAST_TAB]
        binding.rememberLastTabSettingsSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_REMEMBER_LAST_TAB] = checked }

        binding.lightThemeSwitch.isChecked = on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME]
        binding.lightThemeSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME] = checked }

        binding.autoAnswerSwitch.isChecked = on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_AUTO_ANSWER_CALLS]
        binding.autoAnswerSwitch.setOnCheckedChangeListener { _, checked ->
            on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_AUTO_ANSWER_CALLS] = checked

            if (checked) {
                on<DefaultAlerts>().message(R.string.your_auto_answering)
            }
        }

        binding.showCloseButtonSettingsSwitch.isChecked = !on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_HIDE_CLOSE_BUTTON]
        binding.showCloseButtonSettingsSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_HIDE_CLOSE_BUTTON] = !checked }

        binding.largeMapBubblesSwitch.isChecked = on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_USE_LARGE_MAP_BUBBLES]
        binding.largeMapBubblesSwitch.setOnCheckedChangeListener { _, checked -> on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_USE_LARGE_MAP_BUBBLES] = checked }

        binding.sendFeedbackButton.setOnClickListener { v -> on<GroupActivityTransitionHandler>().showGroupMessages(v, on<ConfigHandler>().feedbackGroupId()) }
        binding.viewPrivacyPolicyButton.setOnClickListener { on<DisposableHandler>().add(on<ApiHandler>().privacy().subscribe({ privacyPolicy -> on<DefaultAlerts>().message(privacyPolicy) }, { on<DefaultAlerts>().thatDidntWork() })) }
        binding.viewTermsOfUseButton.setOnClickListener { on<DisposableHandler>().add(on<ApiHandler>().terms().subscribe({ terms -> on<DefaultAlerts>().message(terms) }, { on<DefaultAlerts>().thatDidntWork() })) }
        binding.returnToMapButton.setOnClickListener { on<MapActivityHandler>().goToScreen(MapsActivity.EXTRA_SCREEN_MAP) }

        val publicNotificationsSettingsSwitch = view.findViewById<Switch>(R.id.publicNotificationsSettingsSwitch)

        binding.publicNotificationsSettingsSwitch.setOnCheckedChangeListener { _, isChecked ->
            on<Animate>().alpha(binding.publicNotificationsSettingsSwitchDescription, !isChecked)
            on<AccountHandler>().updatePrivateMode(!isChecked)
        }

        binding.publicNotificationsSettingsSwitch.isChecked = !on<AccountHandler>().privateMode
        binding.publicNotificationsSettingsSwitchDescription.visible = !publicNotificationsSettingsSwitch.isChecked

        binding.featureRequestsButton.setOnClickListener {
            on<FeatureRequestsHandler>().show()
        }

        binding.kioskModeButton.setOnClickListener {
            on<DefaultAlerts>().message("Coming soon!")
        }

        binding.appVersion.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

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
