package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActionBarButton
import closer.vlllage.com.closer.handler.group.MyGroupsAdapter
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import com.queatz.on.On

class MyGroupsLayoutActionsHandler constructor(private val on: On) {

    private var myGroupsAdapter: MyGroupsAdapter? = null

    private val actions = mutableListOf<GroupActionBarButton>()

    private var meetPeopleButton: GroupActionBarButton? = null
    private var verifyYourNumberButton: GroupActionBarButton? = null
    private var unmuteNotificationsButton: GroupActionBarButton? = null
    private var setMyName: GroupActionBarButton? = null

    private val meetPeopleHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = on<ResourcesHandler>().resources.getQuantityString(R.plurals.x_new_people_to_meet, on<MeetHandler>().total.value!!, on<MeetHandler>().total.value.toString())
            meetPeopleButton = GroupActionBarButton(action, { on<MeetHandler>().next() },
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

    private val verifyYourNumberButtonHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = on<ResourcesHandler>().resources.getString(R.string.verify_your_number)
            verifyYourNumberButton = GroupActionBarButton(action, { on<VerifyNumberHandler>().verify() }).also {
                it.icon = R.drawable.ic_smartphone_black_24dp
            }
        }

        override fun get() = verifyYourNumberButton
        override fun unset() {
            verifyYourNumberButton = null
        }
    }

    private val setMyNameHandle = object : GroupActionBarButtonHandle {
        override fun set() {
            val action = on<ResourcesHandler>().resources.getString(R.string.set_my_name)
            setMyName = GroupActionBarButton(action, {
                on<SetNameHandler>().modifyName({
                    if (!it.isNullOrBlank()) {
                        showSetMyName(false)
                        on<NavigationHandler>().showProfile(on<PersistenceHandler>().phoneId!!)
                    }
                }, false)
            }).also {
                it.icon = R.drawable.ic_person_black_24dp
            }
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

    internal fun showSetMyName(show: Boolean) {
        show(setMyNameHandle, show, 0)
    }

    internal fun showMeetPeople(show: Boolean) {
        show(meetPeopleHandle, show, actions.size)
    }

    private fun show(handle: GroupActionBarButtonHandle, show: Boolean, position: Int) {
        var newPosition = position

        if (show) {
            val oldPosition = actions.indexOf(handle.get())
            if (oldPosition != -1) {
                actions.remove(handle.get()!!)
                if (position > oldPosition) {
                    newPosition -= 1
                }
            }

            handle.set()

            if (newPosition < 0) {
                actions.add(handle.get()!!)
            } else {
                actions.add(newPosition, handle.get()!!)
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
