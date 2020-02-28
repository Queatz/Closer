package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.GoalAdapter
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.ReplyHandler
import closer.vlllage.com.closer.pool.PoolActivityFragment
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
            onPhoneChanged(disposableGroup) { phone ->

                activeTextView.text = on<ResourcesHandler>().resources.getString(R.string.last_active, on<TimeStr>().approx(phone.updated))

                val nothing = on<ResourcesHandler>().resources.getString(R.string.nothing_here)
                introductionTextView.text = phone.introduction ?: nothing
                offtimeTextView.text = phone.offtime ?: nothing
                occupationTextView.text = phone.occupation ?: nothing
                historyTextView.text = phone.history ?: nothing

                introductionTextView.setTextColor(if (phone.introduction.isNullOrBlank())
                    on<ResourcesHandler>().resources.getColor(R.color.textHintInverse)
                else
                    on<ResourcesHandler>().resources.getColor(R.color.textInverse))

                offtimeTextView.setTextColor(if (phone.offtime.isNullOrBlank())
                    on<ResourcesHandler>().resources.getColor(R.color.textHintInverse)
                else
                    on<ResourcesHandler>().resources.getColor(R.color.textInverse))

                occupationTextView.setTextColor(if (phone.occupation.isNullOrBlank())
                    on<ResourcesHandler>().resources.getColor(R.color.textHintInverse)
                else
                    on<ResourcesHandler>().resources.getColor(R.color.textInverse))

                historyTextView.setTextColor(if (phone.history.isNullOrBlank())
                    on<ResourcesHandler>().resources.getColor(R.color.textHintInverse)
                else
                    on<ResourcesHandler>().resources.getColor(R.color.textInverse))

                goalsEmptyTextView.visible = phone.goals.isNullOrEmpty()
                lifestylesEmptyTextView.visible = phone.lifestyles.isNullOrEmpty()

                val editable = phone.id == on<PersistenceHandler>().phoneId

                val goalAdapter = GoalAdapter(on) {
                    if (editable) {
                        on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.remove_goal)) { _ ->
                            disposableGroup.add(on<ApiHandler>().addGoal(it, true)
                                    .subscribe({
                                        on<RefreshHandler>().refreshPhone(phone.id!!)
                                    }, { on<DefaultAlerts>().thatDidntWork() }))
                        }
                    } else {
                        on<ReplyHandler>().reply(phone.id!!)
                    }
                }

                goalAdapter.type = on<ResourcesHandler>().resources.getString(R.string.goal)
                goalAdapter.name = on<NameHandler>().getName(phone)
                goalAdapter.items = phone.goals?.toMutableList() ?: mutableListOf()
                goalAdapter.isRemove = editable

                goalsRecyclerView.adapter = goalAdapter
                goalsRecyclerView.layoutManager = LinearLayoutManager(context)

                val lifestyleAdapter = GoalAdapter(on) {
                    if (editable) {
                        on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.remove_lifestyle)) { _ ->
                            disposableGroup.add(on<ApiHandler>().addLifestyle(it, true)
                                    .subscribe({
                                        on<RefreshHandler>().refreshPhone(phone.id!!)
                                    }, { on<DefaultAlerts>().thatDidntWork() }))
                        }
                    } else {
                        on<ReplyHandler>().reply(phone.id!!)
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
                    actionEditIntroduction.setOnClickListener {
                        on<DefaultInput>().show(R.string.introduction_hint, prefill = phone.introduction) {
                            on<AccountHandler>().updateAbout(introduction = it) {
                                on<RefreshHandler>().refreshPhone(phone.id!!)
                            }
                        }
                    }

                    actionAddGoal.setOnClickListener {
                        on<DefaultInput>().show(R.string.add_a_goal) {
                            disposableGroup.add(on<ApiHandler>().addGoal(it)
                                    .subscribe({
                                        on<RefreshHandler>().refreshPhone(phone.id!!)
                                    }, { on<DefaultAlerts>().thatDidntWork() }))
                        }
                    }

                    actionAddLifestyle.setOnClickListener {
                        on<DefaultInput>().show(R.string.add_a_lifestyle) {
                            disposableGroup.add(on<ApiHandler>().addLifestyle(it)
                                    .subscribe({
                                        on<RefreshHandler>().refreshPhone(phone.id!!)
                                    }, { on<DefaultAlerts>().thatDidntWork() }))
                        }
                    }

                    actionEditOfftime.setOnClickListener {
                        on<DefaultInput>().show(R.string.offtime_hint, prefill = phone.offtime) {
                            on<AccountHandler>().updateAbout(offtime = it) {
                                on<RefreshHandler>().refreshPhone(phone.id!!)
                            }
                        }
                    }

                    actionEditOccupation.setOnClickListener {
                        on<DefaultInput>().show(R.string.occupation_hint, prefill = phone.occupation) {
                            on<AccountHandler>().updateAbout(occupation = it) {
                                on<RefreshHandler>().refreshPhone(phone.id!!)
                            }
                        }
                    }

                    actionEditHistory.setOnClickListener {
                        on<DefaultInput>().show(R.string.history_hint, prefill = phone.history) {
                            on<AccountHandler>().updateAbout(history = it) {
                                on<RefreshHandler>().refreshPhone(phone.id!!)
                            }
                        }
                    }

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}
