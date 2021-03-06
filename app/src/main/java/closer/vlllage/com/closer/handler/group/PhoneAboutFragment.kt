package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.call.CallHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.*
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting
import closer.vlllage.com.closer.pool.PoolActivityFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_phone_about.*


class PhoneAboutFragment : PoolActivityFragment() {

    private lateinit var disposableGroup: DisposableGroup
    private lateinit var phoneDisposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_phone_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        phoneDisposableGroup = disposableGroup.group()

        on<GroupHandler> {
            onPhoneUpdated(disposableGroup) { phone ->

                activeTextView.text = on<TimeStr>().lastActive(phone.updated)
                joined.text = on<TimeStr>().joined(phone.created)
                phoneVerifiedTextView.visible = phone.verified ?: false

                (phone.latitude != null && phone.longitude != null).let { hasLocation ->
                    if (hasLocation) {
                        on<ProximityHandler>().locationFromLatLng(LatLng(phone.latitude!!, phone.longitude!!)) {
                            location.text = it
                            location.visible = it.isNullOrBlank().not()
                        }
                    }
                }

                sendDirectMessageButton.visible = phone.id != on<PersistenceHandler>().phoneId

                sendDirectMessageButton.setOnClickListener {
                    on<ReplyHandler>().reply(phone)
                }

                startCallMessageButton.visible = phone.id != on<PersistenceHandler>().phoneId

                startCallMessageButton.setOnClickListener {
                    on<CallHandler>().startCall(phone.id!!)
                }

                val nothing = on<ResourcesHandler>().resources.getString(R.string.nothing_here)
                introductionTextView.text = phone.introduction?.takeIf { it.isNotBlank() } ?: nothing
                offtimeTextView.text = phone.offtime?.takeIf { it.isNotBlank() } ?: nothing
                occupationTextView.text = phone.occupation?.takeIf { it.isNotBlank() } ?: nothing
                historyTextView.text = phone.history?.takeIf { it.isNotBlank() } ?: nothing

                val name = on<NameHandler>().getName(phone)
                goalsHeader.text = on<ResourcesHandler>().resources.getString(R.string.current_goals, name)
                lifestyleHeader.text = on<ResourcesHandler>().resources.getString(R.string.current_lifestyles, name)
                aboutHeader.text = on<ResourcesHandler>().resources.getString(R.string.about_x, name)
                moreAboutHeader.text = on<ResourcesHandler>().resources.getString(R.string.more_about_x, name)

                if (group?.photo.isNullOrEmpty()) {
                    on<LightDarkHandler>().setLight(true)
                } else {
                    on<LightDarkHandler>().setLight(on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME])
                }

                on<LightDarkHandler>().onLightChanged.subscribe {
                    goalsHeader.setTextColor(it.text)
                    lifestyleHeader.setTextColor(it.text)
                    moreAboutHeader.setTextColor(it.text)
                    aboutHeader.setTextColor(it.text)
                }.also {
                    on<DisposableHandler>().add(it)
                }

                with(on<ResourcesHandler>().resources) {
                    introductionTextView.setTextColor(
                            if (phone.introduction.isNullOrBlank()) getColor(R.color.textHintInverse)
                            else getColor(R.color.textInverse)
                    )

                    offtimeTextView.setTextColor(
                            if (phone.offtime.isNullOrBlank()) getColor(R.color.textHintInverse)
                            else getColor(R.color.textInverse)
                    )

                    occupationTextView.setTextColor(
                            if (phone.occupation.isNullOrBlank()) getColor(R.color.textHintInverse)
                            else getColor(R.color.textInverse)
                    )

                    historyTextView.setTextColor(
                            if (phone.history.isNullOrBlank()) getColor(R.color.textHintInverse)
                            else getColor(R.color.textInverse)
                    )
                }

                with({ tv: TextView, text: String? ->
                    tv.setTextColor(on<ResourcesHandler>().resources.getColor(
                            if (text.isNullOrBlank()) R.color.textHintInverse else R.color.textInverse
                    ))
                }) {
                    this(introductionTextView, phone.introduction)
                    this(offtimeTextView, phone.offtime)
                    this(occupationTextView, phone.occupation)
                    this(historyTextView, phone.history)
                }

                goalsEmptyTextView.visible = phone.goals.isNullOrEmpty()
                lifestylesEmptyTextView.visible = phone.lifestyles.isNullOrEmpty()

                val editable = phone.id == on<PersistenceHandler>().phoneId

                val goalAdapter = GoalAdapter(on, false) {
                    if (editable) {
                        on<MenuHandler>().show(
                                MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.remove_goal) {
                                    on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.remove_goal_confirm)) { _ ->
                                        disposableGroup.add(on<ApiHandler>().addGoal(it, true)
                                                .subscribe({
                                                    on<ToastHandler>().show(R.string.goal_removed)
                                                    on<RefreshHandler>().refreshPhone(phone.id!!)
                                                }, { on<DefaultAlerts>().thatDidntWork() }))
                                    }
                                },
                                MenuHandler.MenuOption(R.drawable.ic_group_black_24dp, R.string.see_people_with_goal) {
                                    on<PhoneListActivityTransitionHandler>().showPhonesForGoal(it)
                                }
                        )
                    } else {
                        on<GoalHandler>().show(it, phone)
                    }
                }

                goalAdapter.type = on<ResourcesHandler>().resources.getString(R.string.goal)
                goalAdapter.name = on<NameHandler>().getName(phone)
                goalAdapter.items = phone.goals?.toMutableList() ?: mutableListOf()
                goalAdapter.isRemove = editable

                goalsRecyclerView.adapter = goalAdapter
                goalsRecyclerView.layoutManager = LinearLayoutManager(context)

                val lifestyleAdapter = GoalAdapter(on, true) {
                    if (editable) {
                        on<MenuHandler>().show(
                                MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.remove_lifestyle) {
                                    on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.remove_lifestyle_confirm)) { _ ->
                                        disposableGroup.add(on<ApiHandler>().addLifestyle(it, true)
                                                .subscribe({
                                                    on<ToastHandler>().show(R.string.lifestyle_removed)
                                                    on<RefreshHandler>().refreshPhone(phone.id!!)
                                                }, { on<DefaultAlerts>().thatDidntWork() }))
                                    }
                                },
                                MenuHandler.MenuOption(R.drawable.ic_group_black_24dp, R.string.see_people_with_lifestyle) {
                                    on<PhoneListActivityTransitionHandler>().showPhonesForLifestyle(it)
                                }
                        )
                    } else {
                        on<LifestyleHandler>().show(it, phone)
                    }
                }

                lifestyleAdapter.type = on<ResourcesHandler>().resources.getString(R.string.lifestyle)
                lifestyleAdapter.name = on<NameHandler>().getName(phone)
                lifestyleAdapter.items = phone.lifestyles?.toMutableList() ?: mutableListOf()
                lifestyleAdapter.isRemove = editable

                lifestyleRecyclerView.adapter = lifestyleAdapter
                lifestyleRecyclerView.layoutManager = LinearLayoutManager(context)

                actionEditIntroduction.visible = editable
                actionAddGoal.visible = editable
                actionAddLifestyle.visible = editable
                actionEditOfftime.visible = editable
                actionEditOccupation.visible = editable
                actionEditHistory.visible = editable

                if (editable) {
                    actionEditIntroduction.setOnClickListener { on<ProfileHelper>().editIntroduction(phone) }
                    actionAddGoal.setOnClickListener { on<ProfileHelper>().addGoal(phone.id!!) }
                    actionAddLifestyle.setOnClickListener { on<ProfileHelper>().joinLifestyle(phone.id!!) }
                    actionEditOfftime.setOnClickListener { on<ProfileHelper>().editOfftime(phone) }
                    actionEditOccupation.setOnClickListener { on<ProfileHelper>().editOccupation(phone) }
                    actionEditHistory.setOnClickListener { on<ProfileHelper>().editHistory(phone) }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}
