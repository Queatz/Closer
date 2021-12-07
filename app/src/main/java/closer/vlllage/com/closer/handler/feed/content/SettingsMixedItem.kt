package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.ViewGroup
import closer.vlllage.com.closer.BuildConfig
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.ActivitySettingsBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.featurerequests.FeatureRequestsHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.map.SetNameHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.handler.settings.ConfigHandler
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting
import closer.vlllage.com.closer.ui.Animate
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.queatz.on.On

class SettingsMixedItem(val text: String, val cta: String? = null, val callback: (() -> Unit)? = null) : MixedItem(MixedItemType.Welcome)

class WelcomeViewHolder(val binding: ActivitySettingsBinding) : MixedItemViewHolder(binding.root, MixedItemType.Welcome) {
    var previousStatus: String? = null
}

class WelcomeMixedItemAdapter(private val on: On) : MixedItemAdapter<SettingsMixedItem, WelcomeViewHolder> {
    override fun bind(holder: WelcomeViewHolder, item: SettingsMixedItem, position: Int) {
        bindWelcome(holder, item)
    }

    override fun getMixedItemClass() = SettingsMixedItem::class
    override fun getMixedItemType() = MixedItemType.Welcome

    override fun areItemsTheSame(old: SettingsMixedItem, new: SettingsMixedItem) = false

    override fun areContentsTheSame(old: SettingsMixedItem, new: SettingsMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = WelcomeViewHolder(ActivitySettingsBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onViewRecycled(holder: WelcomeViewHolder) {
    }

    private fun bindWelcome(holder: WelcomeViewHolder, item: SettingsMixedItem) {
        val binding = holder.binding

        // Profile

        holder.previousStatus = on<AccountHandler>().status
        binding.currentStatus.setText(holder.previousStatus)

        binding.currentStatus.setOnFocusChangeListener { _, _ ->
            if (binding.currentStatus.text.toString() == holder.previousStatus) {
                return@setOnFocusChangeListener
            }

            on<AccountHandler>().updateStatus(binding.currentStatus.text.toString())
            on<KeyboardHandler>().showKeyboard(binding.currentStatus, false)
        }

        binding.yourName.text = on<Val>().of(on<AccountHandler>().name, on<ResourcesHandler>().resources.getString(R.string.update_your_name))

        binding.yourPhoto.setOnClickListener {
            on<DefaultMenus>().uploadPhoto { photoId ->
                val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)
                on<AccountHandler>().updatePhoto(photo)
            }
        }

        if (on<PersistenceHandler>().myPhoto.isNotBlank()) {
            on<ImageHandler>().get().load(on<PersistenceHandler>().myPhoto + "?s=128")
                .apply(RequestOptions().circleCrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.yourPhoto)
        }

        on<DisposableHandler>().add(on<AccountHandler>().changes().subscribe(
            { accountChange ->
                if (accountChange.prop == AccountHandler.ACCOUNT_FIELD_NAME) {
                    binding.yourName.text = on<AccountHandler>().name
                }
                if (accountChange.prop == AccountHandler.ACCOUNT_FIELD_PHOTO) {
                    on<PhotoHelper>().loadCircle(binding.yourPhoto, on<PersistenceHandler>().myPhoto + "?s=128")
                }
            },
            { on<DefaultAlerts>().thatDidntWork() }
        ))

        binding.yourName.setOnClickListener { v -> on<SetNameHandler>().modifyName() }

        binding.actionViewProfile.setOnClickListener {
            on<NavigationHandler>().showMyProfile(binding.actionViewProfile)
        }

        // Settings

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
        binding.returnToMapButton.setOnClickListener { on<MapActivityHandler>().goToMap() }

        binding.publicNotificationsSettingsSwitch.setOnCheckedChangeListener { _, isChecked ->
            on<Animate>().alpha(binding.publicNotificationsSettingsSwitchDescription, !isChecked)
            on<AccountHandler>().updatePrivateMode(!isChecked)
        }

        binding.publicNotificationsSettingsSwitch.isChecked = !on<AccountHandler>().privateMode
        binding.publicNotificationsSettingsSwitchDescription.visible = !binding.publicNotificationsSettingsSwitch.isChecked

        binding.featureRequestsButton.setOnClickListener {
            on<FeatureRequestsHandler>().show()
        }

        binding.kioskModeButton.setOnClickListener {
            on<DefaultAlerts>().message("Coming soon!")
        }

        binding.appVersion.text = "${on<ResourcesHandler>().resources.getString(R.string.version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

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