package closer.vlllage.com.closer.handler.map

import android.view.View
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.featurerequests.FeatureRequestsHandler
import closer.vlllage.com.closer.handler.group.GroupActionBarButton
import closer.vlllage.com.closer.handler.group.MyGroupsAdapter
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.settings.HelpHandler
import com.queatz.on.On
import java.util.*

class MyGroupsLayoutActionsHandler constructor(private val on: On) {

    private var myGroupsAdapter: MyGroupsAdapter? = null

    private val actions = ArrayList<GroupActionBarButton>()

    private var meetPeopleButton: GroupActionBarButton? = null
    private var featureRequestsButton: GroupActionBarButton? = null
    private var verifyYourNumberButton: GroupActionBarButton? = null
    private var allowPermissionsButton: GroupActionBarButton? = null
    private var unmuteNotificationsButton: GroupActionBarButton? = null
    private var showHelpButton: GroupActionBarButton? = null
    private var setMyName: GroupActionBarButton? = null

    private val meetPeopleHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = on<ResourcesHandler>().resources.getQuantityString(R.plurals.x_new_people_to_meet, on<MeetHandler>().total.value!!, on<MeetHandler>().total.value.toString())
            meetPeopleButton = GroupActionBarButton(action, View.OnClickListener { on<MeetHandler>().next() },
                    backgroundDrawableRes = R.drawable.clickable_white_rounded,
                    textColorRes = R.color.textInverse).also {
                it.icon = R.drawable.ic_person_black_24dp
            }
        }

        override fun get() = meetPeopleButton
        override fun unset() {
            meetPeopleButton = null
        }
    }

    private val featureRequestsHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            featureRequestsButton = GroupActionBarButton(on<ResourcesHandler>().resources.getString(R.string.feature_requests), View.OnClickListener { on<FeatureRequestsHandler>().show() },
                    backgroundDrawableRes = R.drawable.clickable_red,
                    textColorRes = R.color.text).also {
                it.icon = R.drawable.ic_star_black_24dp
            }
        }

        override fun get() = featureRequestsButton
        override fun unset() {
            featureRequestsButton = null
        }
    }

    private val verifyYourNumberButtonHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = on<ResourcesHandler>().resources.getString(R.string.verify_your_number)
            verifyYourNumberButton = GroupActionBarButton(action, View.OnClickListener { on<VerifyNumberHandler>().verify() })
        }

        override fun get() = verifyYourNumberButton
        override fun unset() {
            verifyYourNumberButton = null
        }
    }

    private val allowPermissionsButtonHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = on<ResourcesHandler>().resources.getString(R.string.use_your_location)
            allowPermissionsButton = GroupActionBarButton(action, View.OnClickListener { view ->
                on<AlertHandler>().make().apply {
                    title = on<ResourcesHandler>().resources.getString(R.string.enable_location_permission)
                    message = on<ResourcesHandler>().resources.getString(R.string.enable_location_permission_rationale)
                    positiveButton = on<ResourcesHandler>().resources.getString(R.string.open_settings)
                    positiveButtonCallback = { alertResult -> on<SystemSettingsHandler>().showSystemSettings() }
                }
            })
        }

        override fun get() = allowPermissionsButton
        override fun unset() {
            allowPermissionsButton = null
        }
    }

    private val unmuteNotificationsButtonHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = on<ResourcesHandler>().resources.getString(R.string.unmute_notifications)
            unmuteNotificationsButton = GroupActionBarButton(action, View.OnClickListener {
                on<PersistenceHandler>().isNotificationsPaused = false
                on<ToastHandler>().show(R.string.notifications_on)
                actions.remove(unmuteNotificationsButton)
                myGroupsAdapter!!.setActions(actions)
            })
        }

        override fun get() = unmuteNotificationsButton
        override fun unset() {
            unmuteNotificationsButton = null
        }
    }

    private val showHelpButtonHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = on<ResourcesHandler>().resources.getString(R.string.show_help)
            showHelpButton = GroupActionBarButton(action, View.OnClickListener { on<HelpHandler>().showHelp() }, View.OnClickListener {
                on<PersistenceHandler>().isHelpHidden = true
                on<AlertHandler>().make().apply {
                    message = on<ResourcesHandler>().resources.getString(R.string.you_hid_the_help_bubble)
                    positiveButton = on<ResourcesHandler>().resources.getString(R.string.ok)
                    show()
                }
                showHelpButton(false)
            }, R.drawable.clickable_green_light)
        }

        override fun get() = showHelpButton
        override fun unset() {
            showHelpButton = null
        }
    }

    private val setMyNameHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = on<ResourcesHandler>().resources.getString(R.string.set_my_name)
            setMyName = GroupActionBarButton(action, View.OnClickListener {
                on<SetNameHandler>().modifyName(object : SetNameHandler.OnNameModifiedCallback {
                    override fun onNameModified(name: String?) {
                        if (!on<Val>().isEmpty(name)) {
                            showSetMyName(false)
                        }
                    }
                }, false)
            })
        }

        override fun get() = setMyName
        override fun unset() {
            setMyName = null
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

    internal fun showMeetPeople(show: Boolean) {
        show(meetPeopleHandle, show, actions.size)
    }

    fun showFeatureRequests(show: Boolean) {
        show(featureRequestsHandle, show, 0)
    }

    private fun show(handle: GroupActionBarButtonHandle, show: Boolean, position: Int) {
        if (show) {
            if (handle.get() != null) {
                actions.remove(handle.get()!!)
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
            handle.unset()
        }

        myGroupsAdapter!!.setActions(actions)
    }

    internal interface GroupActionBarButtonHandle {
        fun set()
        fun get(): GroupActionBarButton?
        fun unset()
    }
}
