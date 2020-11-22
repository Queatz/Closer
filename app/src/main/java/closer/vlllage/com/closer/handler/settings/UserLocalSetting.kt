package closer.vlllage.com.closer.handler.settings

enum class UserLocalSetting(val defaultValue: Boolean) {
    CLOSER_SETTINGS_OPEN_GROUP_EXPANDED(true),
    CLOSER_SETTINGS_OPEN_FEED_EXPANDED(false),
    CLOSER_SETTINGS_HIDE_CLOSE_BUTTON(false),
    CLOSER_SETTINGS_USE_LIGHT_THEME(true),
    CLOSER_SETTINGS_AUTO_ANSWER_CALLS(false),
}
