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
import at.bluesource.choicesdk.maps.common.LatLng
import closer.vlllage.com.closer.databinding.FragmentPhoneAboutBinding


class PhoneAboutFragment : PoolActivityFragment() {

    private lateinit var binding: FragmentPhoneAboutBinding
    private lateinit var disposableGroup: DisposableGroup
    private lateinit var phoneDisposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentPhoneAboutBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        phoneDisposableGroup = disposableGroup.group()

        on<GroupHandler> {
            onPhoneUpdated(disposableGroup) { phone ->

                binding.activeTextView.text = on<TimeStr>().lastActive(phone.updated)
                binding.joined.text = on<TimeStr>().joined(phone.created)
                binding.phoneVerifiedTextView.visible = phone.verified ?: false

                (phone.latitude != null && phone.longitude != null).let { hasLocation ->
                    if (hasLocation) {
                        on<ProximityHandler>().locationFromLatLng(LatLng(phone.latitude!!, phone.longitude!!)) {
                            binding.location.text = it
                            binding.location.visible = it.isNullOrBlank().not()
                        }
                    }
                }

                binding.sendDirectMessageButton.visible = phone.id != on<PersistenceHandler>().phoneId

                binding.sendDirectMessageButton.setOnClickListener {
                    on<ReplyHandler>().reply(phone)
                }

                binding.startCallMessageButton.visible = phone.id != on<PersistenceHandler>().phoneId

                binding.startCallMessageButton.setOnClickListener {
                    on<CallHandler>().startCall(phone.id!!)
                }

                val nothing = on<ResourcesHandler>().resources.getString(R.string.nothing_here)
                binding.introductionTextView.text = phone.introduction?.takeIf { it.isNotBlank() } ?: nothing
                binding.offtimeTextView.text = phone.offtime?.takeIf { it.isNotBlank() } ?: nothing
                binding.occupationTextView.text = phone.occupation?.takeIf { it.isNotBlank() } ?: nothing
                binding.historyTextView.text = phone.history?.takeIf { it.isNotBlank() } ?: nothing

                val name = on<NameHandler>().getName(phone)
                binding.goalsHeader.text = on<ResourcesHandler>().resources.getString(R.string.current_goals, name)
                binding.lifestyleHeader.text = on<ResourcesHandler>().resources.getString(R.string.current_lifestyles, name)
                binding.aboutHeader.text = on<ResourcesHandler>().resources.getString(R.string.about_x, name)
                binding.moreAboutHeader.text = on<ResourcesHandler>().resources.getString(R.string.more_about_x, name)

                if (group?.photo.isNullOrEmpty()) {
                    on<LightDarkHandler>().setLight(true)
                } else {
                    on<LightDarkHandler>().setLight(on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME])
                }

                on<LightDarkHandler>().onLightChanged.subscribe {
                    binding.goalsHeader.setTextColor(it.text)
                    binding.lifestyleHeader.setTextColor(it.text)
                    binding.moreAboutHeader.setTextColor(it.text)
                    binding.aboutHeader.setTextColor(it.text)
                }.also {
                    on<DisposableHandler>().add(it)
                }

                with(on<ResourcesHandler>().resources) {
                    binding.introductionTextView.setTextColor(
                            if (phone.introduction.isNullOrBlank()) getColor(R.color.textHintInverse)
                            else getColor(R.color.textInverse)
                    )

                    binding.offtimeTextView.setTextColor(
                            if (phone.offtime.isNullOrBlank()) getColor(R.color.textHintInverse)
                            else getColor(R.color.textInverse)
                    )

                    binding.occupationTextView.setTextColor(
                            if (phone.occupation.isNullOrBlank()) getColor(R.color.textHintInverse)
                            else getColor(R.color.textInverse)
                    )

                    binding.historyTextView.setTextColor(
                            if (phone.history.isNullOrBlank()) getColor(R.color.textHintInverse)
                            else getColor(R.color.textInverse)
                    )
                }

                with({ tv: TextView, text: String? ->
                    tv.setTextColor(on<ResourcesHandler>().resources.getColor(
                            if (text.isNullOrBlank()) R.color.textHintInverse else R.color.textInverse
                    ))
                }) {
                    this(binding.introductionTextView, phone.introduction)
                    this(binding.offtimeTextView, phone.offtime)
                    this(binding.occupationTextView, phone.occupation)
                    this(binding.historyTextView, phone.history)
                }

                binding.goalsEmptyTextView.visible = phone.goals.isNullOrEmpty()
                binding.lifestylesEmptyTextView.visible = phone.lifestyles.isNullOrEmpty()

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

                binding.goalsRecyclerView.adapter = goalAdapter
                binding.goalsRecyclerView.layoutManager = LinearLayoutManager(context)

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

                binding.lifestyleRecyclerView.adapter = lifestyleAdapter
                binding.lifestyleRecyclerView.layoutManager = LinearLayoutManager(context)

                binding.actionEditIntroduction.visible = editable
                binding.actionAddGoal.visible = editable
                binding.actionAddLifestyle.visible = editable
                binding.actionEditOfftime.visible = editable
                binding.actionEditOccupation.visible = editable
                binding.actionEditHistory.visible = editable

                if (editable) {
                    binding.actionEditIntroduction.setOnClickListener { on<ProfileHelper>().editIntroduction(phone) }
                    binding.actionAddGoal.setOnClickListener { on<ProfileHelper>().addGoal(phone.id!!) }
                    binding.actionAddLifestyle.setOnClickListener { on<ProfileHelper>().joinLifestyle(phone.id!!) }
                    binding.actionEditOfftime.setOnClickListener { on<ProfileHelper>().editOfftime(phone) }
                    binding.actionEditOccupation.setOnClickListener { on<ProfileHelper>().editOccupation(phone) }
                    binding.actionEditHistory.setOnClickListener { on<ProfileHelper>().editHistory(phone) }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}
