package closer.vlllage.com.closer.handler.map

import android.view.View
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActionBarButton
import closer.vlllage.com.closer.handler.group.MyGroupsAdapter
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.settings.HelpHandler
import closer.vlllage.com.closer.pool.PoolMember
import java.util.*

class MyGroupsLayoutActionsHandler : PoolMember() {

    private var myGroupsAdapter: MyGroupsAdapter? = null

    private val actions = ArrayList<GroupActionBarButton>()

    private var verifyYourNumberButton: GroupActionBarButton? = null
    private var allowPermissionsButton: GroupActionBarButton? = null
    private var unmuteNotificationsButton: GroupActionBarButton? = null
    private var showHelpButton: GroupActionBarButton? = null
    private var setMyName: GroupActionBarButton? = null

    private val verifyYourNumberButtonHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = `$`(ResourcesHandler::class.java).resources.getString(R.string.verify_your_number)
            verifyYourNumberButton = GroupActionBarButton(action, View.OnClickListener { view -> `$`(VerifyNumberHandler::class.java).verify() })
        }

        override fun get(): GroupActionBarButton? {
            return verifyYourNumberButton
        }
    }

    private val allowPermissionsButtonHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = `$`(ResourcesHandler::class.java).resources.getString(R.string.use_your_location)
            allowPermissionsButton = GroupActionBarButton(action, View.OnClickListener { view ->
                `$`(AlertHandler::class.java).make().apply {
                    title = `$`(ResourcesHandler::class.java).resources.getString(R.string.enable_location_permission)
                    message = `$`(ResourcesHandler::class.java).resources.getString(R.string.enable_location_permission_rationale)
                    positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.open_settings)
                    positiveButtonCallback = { alertResult -> `$`(SystemSettingsHandler::class.java).showSystemSettings() }
                }
            })
        }

        override fun get(): GroupActionBarButton? {
            return allowPermissionsButton
        }
    }

    private val unmuteNotificationsButtonHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = `$`(ResourcesHandler::class.java).resources.getString(R.string.unmute_notifications)
            unmuteNotificationsButton = GroupActionBarButton(action, View.OnClickListener{  view ->
                `$`(PersistenceHandler::class.java).isNotificationsPaused = false
                `$`(ToastHandler::class.java).show(R.string.notifications_on)
                actions.remove(unmuteNotificationsButton)
                myGroupsAdapter!!.setActions(actions)
            })
        }

        override fun get(): GroupActionBarButton? {
            return unmuteNotificationsButton
        }
    }

    private val showHelpButtonHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = `$`(ResourcesHandler::class.java).resources.getString(R.string.show_help)
            showHelpButton = GroupActionBarButton(action, View.OnClickListener { view -> `$`(HelpHandler::class.java).showHelp() }, View.OnClickListener { view ->
                `$`(PersistenceHandler::class.java).isHelpHidden = true
                `$`(AlertHandler::class.java).make().apply {
                    message = `$`(ResourcesHandler::class.java).resources.getString(R.string.you_hid_the_help_bubble)
                    positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.ok)
                    show()
                }
                showHelpButton(false)
            }, R.drawable.clickable_green_light)
        }

        override fun get(): GroupActionBarButton? {
            return showHelpButton
        }
    }

    private val setMyNameHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = `$`(ResourcesHandler::class.java).resources.getString(R.string.set_my_name)
            setMyName = GroupActionBarButton(action, View.OnClickListener { view ->
                `$`(SetNameHandler::class.java).modifyName(object : SetNameHandler.OnNameModifiedCallback {
                    override fun onNameModified(name: String?) {
                        if (!`$`(Val::class.java).isEmpty(name)) {
                            showSetMyName(false)
                        }
                    }
                }, false)
            })
        }

        override fun get(): GroupActionBarButton? {
            return setMyName
        }
    }

    fun attach(myGroupsAdapter: MyGroupsAdapter) {
        this.myGroupsAdapter = myGroupsAdapter
    }

    internal fun showVerifyMyNumber(show: Boolean) {
        show(verifyYourNumberButtonHandle, show, -1)
    }

    internal fun showAllowLocationPermissionsInSettings(show: Boolean) {
        show(allowPermissionsButtonHandle, show, 0)
    }

    internal fun showUnmuteNotifications(show: Boolean) {
        show(unmuteNotificationsButtonHandle, show, 0)
    }

    internal fun showHelpButton(show: Boolean) {
        show(showHelpButtonHandle, show, 0)
    }

    internal fun showSetMyName(show: Boolean) {
        show(setMyNameHandle, show, 0)
    }

    private fun show(handle: GroupActionBarButtonHandle, show: Boolean, position: Int) {
        if (show) {
            if (handle.get() != null) {
                return
            }

            handle.set()
            if (position < 0) {
                actions.add(handle.get()!!)
            } else {
                actions.add(position, handle.get()!!)
            }
        } else {
            if (handle.get() == null) {
                return
            }

            actions.remove(handle.get()!!)
        }

        myGroupsAdapter!!.setActions(actions)
    }

    internal interface GroupActionBarButtonHandle {
        fun set()
        fun get(): GroupActionBarButton?
    }
}
